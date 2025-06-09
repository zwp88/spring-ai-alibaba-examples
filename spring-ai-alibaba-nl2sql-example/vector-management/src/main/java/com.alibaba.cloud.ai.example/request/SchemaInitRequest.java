package com.alibaba.cloud.ai.example.request;

import com.alibaba.cloud.ai.dbconnector.DbConfig;

import java.io.Serializable;
import java.util.List;

public class SchemaInitRequest implements Serializable {
    private DbConfig dbConfig;
    private List<String> tables;

    public DbConfig getDbConfig() {
        return dbConfig;
    }

    public void setDbConfig(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public List<String> getTables() {
        return tables;
    }

    public void setTables(List<String> tables) {
        this.tables = tables;
    }
}