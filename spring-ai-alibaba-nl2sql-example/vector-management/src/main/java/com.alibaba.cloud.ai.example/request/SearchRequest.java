package com.alibaba.cloud.ai.example.request;


import java.io.Serializable;

public class SearchRequest implements Serializable {
    private String query;
    private int topK;

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getVectorType() {
        return vectorType;
    }

    public void setVectorType(String vectorType) {
        this.vectorType = vectorType;
    }

    private String vectorType;
}
