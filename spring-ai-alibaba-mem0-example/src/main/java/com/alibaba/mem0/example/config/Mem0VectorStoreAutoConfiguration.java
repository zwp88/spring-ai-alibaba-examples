//package com.alibaba.mem0.example.config;
//
//import com.alibaba.example.chatmemory.config.Mem0ChatMemoryAutoConfiguration;
//import com.alibaba.example.chatmemory.config.Mem0ChatMemoryProperties;
//import com.alibaba.example.chatmemory.mem0.Mem0MemoryStore;
//import com.alibaba.example.chatmemory.mem0.Mem0ServiceClient;
//import org.springframework.ai.embedding.EmbeddingModel;
//import org.springframework.boot.autoconfigure.AutoConfiguration;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.web.client.RestClient;
//
//@AutoConfiguration(
//        after = {Mem0ChatMemoryAutoConfiguration.class}
//)
//@ConditionalOnClass({Mem0MemoryStore.class, EmbeddingModel.class, RestClient.class})
//@EnableConfigurationProperties({Mem0ChatMemoryProperties.class})
//@ConditionalOnProperty(
//        name = {"mem0"},
//        havingValue = "client",
//        matchIfMissing = true
//)
//public class Mem0VectorStoreAutoConfiguration {
//    @Bean
//    @ConditionalOnMissingBean
//    Mem0MemoryStore vectorStore(Mem0ServiceClient client) {
//        return Mem0MemoryStore.builder(client).build();
//    }
//}
