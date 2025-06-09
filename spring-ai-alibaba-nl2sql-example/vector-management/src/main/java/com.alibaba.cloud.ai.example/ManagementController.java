package com.alibaba.cloud.ai.example;

import com.alibaba.cloud.ai.analyticdb.AnalyticDbVectorStoreProperties;
import com.alibaba.cloud.ai.dbconnector.DbAccessor;
import com.alibaba.cloud.ai.dbconnector.DbConfig;
import com.alibaba.cloud.ai.dbconnector.bo.ColumnInfoBO;
import com.alibaba.cloud.ai.dbconnector.bo.DbQueryParameter;
import com.alibaba.cloud.ai.dbconnector.bo.ForeignKeyInfoBO;
import com.alibaba.cloud.ai.dbconnector.bo.TableInfoBO;
import com.alibaba.cloud.ai.example.request.DeleteRequest;
import com.alibaba.cloud.ai.example.request.EvidenceRequest;
import com.alibaba.cloud.ai.example.request.SchemaInitRequest;
import com.alibaba.cloud.ai.example.request.SearchRequest;
import com.alibaba.cloud.ai.service.VectorStoreService;
import com.aliyun.gpdb20160503.Client;
import com.aliyun.gpdb20160503.models.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class ManagementController {

    private static final String CONTENT_FIELD_NAME = "content";

    private static final String METADATA_FIELD_NAME = "metadata";

    @Autowired
    @Qualifier("dashscopeEmbeddingModel")
    private EmbeddingModel embeddingModel;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private DbAccessor dbAccessor;

    @Autowired
    private AnalyticDbVectorStoreProperties analyticDbVectorStoreProperties;

    @Autowired
    private Client client;

    @Autowired
    private Gson gson;

    @Autowired
    private VectorStoreService vectorStoreService;

    @PostMapping("/add/evidence")
    public Boolean addEvidence(@RequestBody List<EvidenceRequest> evidenceRequests) {
        List<Document> evidences = new ArrayList<>();
        for (EvidenceRequest req : evidenceRequests) {
            Document doc = new Document(
                    UUID.randomUUID().toString(),
                    req.getContent(),
                    Map.of("evidenceType", req.getType(), "vectorType", "evidence")
            );
            evidences.add(doc);
        }
        vectorStore.add(evidences);
        return true;
    }

    public List<Double> embed(String text) {
        float[] embedded = embeddingModel.embed(text);
        List<Double> result = new ArrayList<>();
        for (float value : embedded) {
            result.add((double) value);
        }
        return result;
    }

    @PostMapping("/search")
    public List<Document> search(@RequestBody SearchRequest searchRequestDTO) {
        String filterTemplate = "jsonb_extract_path_text(metadata, 'vectorType') = '%s'";
        String filterFormatted = String.format(filterTemplate, searchRequestDTO.getVectorType());

        QueryCollectionDataRequest request = new QueryCollectionDataRequest()
                .setDBInstanceId(analyticDbVectorStoreProperties.getDbInstanceId())
                .setRegionId(analyticDbVectorStoreProperties.getRegionId())
                .setNamespace(analyticDbVectorStoreProperties.getNamespace())
                .setNamespacePassword(analyticDbVectorStoreProperties.getNamespacePassword())
                .setCollection(analyticDbVectorStoreProperties.getCollectName())
                .setIncludeValues(false)
                .setMetrics(analyticDbVectorStoreProperties.getMetrics())
                .setVector(embed(searchRequestDTO.getQuery()))
                .setContent(searchRequestDTO.getQuery())
                .setTopK((long) searchRequestDTO.getTopK())
                .setFilter(filterFormatted);
        try {
            QueryCollectionDataResponse response = this.client.queryCollectionData(request);
            List<Document> documents = new ArrayList<>();
            for (QueryCollectionDataResponseBody.QueryCollectionDataResponseBodyMatchesMatch match : response.getBody()
                    .getMatches()
                    .getMatch()) {
                if (match.getScore() != null && match.getScore() > 0.5) {
                    Map<String, String> metadata = match.getMetadata();
                    String pageContent = metadata.get(CONTENT_FIELD_NAME);
                    Map<String, Object> metadataJson = new ObjectMapper().readValue(metadata.get(METADATA_FIELD_NAME),
                            new TypeReference<HashMap<String, Object>>() {
                            });
                    Document doc = new Document(match.getId(), pageContent, metadataJson);
                    documents.add(doc);
                }
            }
            return documents;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/delete")
    public Boolean deleteDocuments(@RequestBody DeleteRequest deleteRequest) {
        try {
            String filterExpression;
            if (deleteRequest.getId() != null && !deleteRequest.getId().isEmpty()) {
                filterExpression = String.format("id = '%s'", deleteRequest.getId());
            } else if (deleteRequest.getVectorType() != null && !deleteRequest.getVectorType().isEmpty()) {
                filterExpression = String.format("jsonb_extract_path_text(metadata, 'vectorType') = '%s'", deleteRequest.getVectorType());
            } else {
                throw new IllegalArgumentException("Either id or vectorType must be specified.");
            }
            DeleteCollectionDataRequest request = getDeleteCollectionDataRequest(filterExpression);
            DeleteCollectionDataResponse deleteCollectionDataResponse = this.client.deleteCollectionData(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete collection data by filterExpression: " + e.getMessage(), e);
        }
        return true;
    }

    private DeleteCollectionDataRequest getDeleteCollectionDataRequest(String query) {
        return new DeleteCollectionDataRequest()
                .setDBInstanceId(analyticDbVectorStoreProperties.getDbInstanceId())
                .setRegionId(analyticDbVectorStoreProperties.getRegionId())
                .setNamespace(analyticDbVectorStoreProperties.getNamespace())
                .setNamespacePassword(analyticDbVectorStoreProperties.getNamespacePassword())
                .setCollection(analyticDbVectorStoreProperties.getCollectName())
                .setCollectionData(null)
                .setCollectionDataFilter(query);
    }

    @PostMapping("/init/schema")
    public Boolean schema(@RequestBody SchemaInitRequest schemaInitRequest) throws Exception {
        DbConfig dbConfig = schemaInitRequest.getDbConfig();
        DbQueryParameter dqp = DbQueryParameter.from(dbConfig).setSchema(dbConfig.getSchema()).setTables(schemaInitRequest.getTables());

        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.setVectorType("column");
        deleteDocuments(deleteRequest);

        deleteRequest.setVectorType("table");
        deleteDocuments(deleteRequest);

        List<ForeignKeyInfoBO> foreignKeyInfoBOS = dbAccessor.showForeignKeys(dbConfig, dqp);
        Map<String, List<String>> foreignKeyMap = new HashMap<>();
        for (ForeignKeyInfoBO foreignKeyInfoBO : foreignKeyInfoBOS) {
            String fKeyString = foreignKeyInfoBO.getTable() + "." + foreignKeyInfoBO.getColumn() +
                    "=" +
                    foreignKeyInfoBO.getReferencedTable() + "." + foreignKeyInfoBO.getReferencedColumn();
            String tableName = foreignKeyInfoBO.getTable();
            String referTableName = foreignKeyInfoBO.getReferencedTable();
            if (!foreignKeyMap.containsKey(tableName)) {
                foreignKeyMap.put(tableName, new ArrayList<>());
            }
            foreignKeyMap.get(tableName).add(fKeyString);
            if (!foreignKeyMap.containsKey(referTableName)) {
                foreignKeyMap.put(referTableName, new ArrayList<>());
            }
            foreignKeyMap.get(referTableName).add(fKeyString);
        }


        List<TableInfoBO> tableInfoBOS = dbAccessor.fetchTables(dbConfig, dqp);
        for (TableInfoBO tableInfoBO : tableInfoBOS) {
            dqp.setTable(tableInfoBO.getName());
            List<ColumnInfoBO> columnInfoBOS = dbAccessor.showColumns(dbConfig, dqp);
            for(ColumnInfoBO columnInfoBO : columnInfoBOS) {
                dqp.setColumn(columnInfoBO.getName());
                List<String> sampleColumn = dbAccessor.sampleColumn(dbConfig, dqp);
                if (sampleColumn != null) {
                    sampleColumn = sampleColumn.stream()
                            .filter(Objects::nonNull)
                            .distinct()
                            .limit(3)
                            .filter(s -> s.length() <= 100)
                            .collect(Collectors.toList());
                } else {
                    sampleColumn = new ArrayList<>();
                }
                columnInfoBO.setTableName(tableInfoBO.getName());
                columnInfoBO.setSamples(gson.toJson(sampleColumn));
            }
            List<Document> columnDocuments = columnInfoBOS.stream()
                    .map(this::convertToDocument)
                    .collect(Collectors.toList());
            vectorStore.add(columnDocuments);

            ColumnInfoBO primaryColumnDO = columnInfoBOS.stream().filter(item -> item.isPrimary()).findFirst().orElse(new ColumnInfoBO());
            tableInfoBO.setPrimaryKey(primaryColumnDO.getName());
            tableInfoBO.setForeignKey(String.join("、", foreignKeyMap.getOrDefault(tableInfoBO.getName(), new ArrayList<>())));
        }
        List<Document> tableVectorDocuments = tableInfoBOS.stream()
                .map(this::convertTableToDocument)
                .collect(Collectors.toList());

        vectorStore.add(tableVectorDocuments);
        return true;
    }

    private Document convertToDocument(ColumnInfoBO columnInfoBO) {
        String text = columnInfoBO.getDescription() != null && !columnInfoBO.getDescription().isEmpty()
                ? columnInfoBO.getName() + "指的是" + columnInfoBO.getDescription()
                : columnInfoBO.getName();
        Map<String, Object> metadata = Map.of(
                "name", columnInfoBO.getName(),
                "tableName", columnInfoBO.getTableName(),
                "description", columnInfoBO.getDescription(),
                "type", columnInfoBO.getType(),
                "primary", columnInfoBO.isPrimary(),
                "notnull", columnInfoBO.isNotnull(),
                "samples", columnInfoBO.getSamples(),
                "vectorType", "column"
        );
        return new Document(columnInfoBO.getName(), text, metadata);
    }

    private Document convertTableToDocument(TableInfoBO tableInfoBO) {
        String text = tableInfoBO.getDescription() != null && !tableInfoBO.getDescription().isEmpty()
                ? tableInfoBO.getName() + "指的是" + tableInfoBO.getDescription()
                : tableInfoBO.getName();
        Map<String, Object> metadata = Map.of(
                "schema", Optional.ofNullable(tableInfoBO.getSchema()).orElse(""),
                "name", tableInfoBO.getName(),
                "description", tableInfoBO.getDescription(),
                "foreignKey", tableInfoBO.getForeignKey(),
                "primaryKey", tableInfoBO.getPrimaryKey(),
                "vectorType", "table"
        );
        return new Document(tableInfoBO.getName(), text, metadata);
    }


}
