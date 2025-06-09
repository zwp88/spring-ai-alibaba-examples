package com.alibaba.cloud.ai.example;

import com.alibaba.cloud.ai.analyticdb.AnalyticDbConfig;
import com.alibaba.cloud.ai.analyticdb.AnalyticDbVectorStore;
import com.alibaba.cloud.ai.analyticdb.AnalyticDbVectorStoreProperties;
import com.aliyun.gpdb20160503.Client;
import com.aliyun.teaopenapi.models.Config;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = {"com.alibaba.cloud.ai"})
@AutoConfiguration
@ConditionalOnClass({ EmbeddingModel.class, Client.class, AnalyticDbVectorStore.class })
@EnableConfigurationProperties({ AnalyticDbVectorStoreProperties.class })
@ConditionalOnProperty(prefix = "spring.ai.vectorstore.analytic", name = "enabled", havingValue = "true", matchIfMissing = true)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    @ConditionalOnMissingBean
    public Client client(AnalyticDbVectorStoreProperties properties) throws Exception {
        Config clientConfig = Config.build(properties.toAnalyticDbClientParams());
        return new Client(clientConfig);
    }

    @Bean
    @ConditionalOnMissingBean(BatchingStrategy.class)
    BatchingStrategy batchingStrategy() {
        return new TokenCountBatchingStrategy();
    }

    @Bean
    @ConditionalOnMissingBean
    public AnalyticDbVectorStore vectorStore(Client client, EmbeddingModel embeddingModel,
                                             AnalyticDbVectorStoreProperties properties, ObjectProvider<ObservationRegistry> observationRegistry,
                                             ObjectProvider<VectorStoreObservationConvention> customObservationConvention,
                                             BatchingStrategy batchingStrategy) {

        AnalyticDbConfig config = new AnalyticDbConfig().setAccessKeyId(properties.getAccessKeyId())
                .setAccessKeySecret(properties.getAccessKeySecret())
                .setRegionId(properties.getRegionId())
                .setDbInstanceId(properties.getDbInstanceId())
                .setManagerAccount(properties.getManagerAccount())
                .setManagerAccountPassword(properties.getManagerAccountPassword())
                .setNamespace(properties.getNamespace())
                .setNamespacePassword(properties.getNamespacePassword());
        if (properties.getMetrics() != null) {
            config.setMetrics(properties.getMetrics());
        }
        if (properties.getReadTimeout() != null) {
            config.setReadTimeout(properties.getReadTimeout());
        }
        if (properties.getUserAgent() != null) {
            config.setUserAgent(properties.getUserAgent());
        }
        var builder = AnalyticDbVectorStore.builder(properties.getCollectName(), config, client, embeddingModel)
                .batchingStrategy(batchingStrategy)
                .observationRegistry(observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP))
                .customObservationConvention(customObservationConvention.getIfAvailable(() -> null));
        if (properties.getDefaultTopK() >= 0) {
            builder.defaultTopK(properties.getDefaultTopK());
        }

        if (properties.getDefaultSimilarityThreshold() >= 0.0) {
            builder.defaultSimilarityThreshold(properties.getDefaultSimilarityThreshold());
        }
        return builder.build();
    }

}
