/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.touhouqing.chatAiDemo.entity;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 供应商实体
 */
@Node("Supplier")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Property("name")
    private String name;
    
    @Property("companyType")
    private String companyType; // 有限公司、股份公司等
    
    @Property("registrationNumber")
    private String registrationNumber; // 注册号
    
    @Property("address")
    private String address;
    
    @Property("contact")
    private String contact;
    
    @Property("businessScope")
    private String businessScope;
    
    @Property("registeredCapital")
    private Double registeredCapital;
    
    @Property("establishedDate")
    private String establishedDate;
    
    @Property("creditRating")
    private String creditRating;
    
    @Property("createdAt")
    private LocalDateTime createdAt;
    
    @Property("updatedAt")
    private LocalDateTime updatedAt;
    
    // 与采购项目的关系
    @Relationship(type = "AWARDED_TO", direction = Relationship.Direction.INCOMING)
    private List<ProcurementProject> awardedProjects;
    
    // 与行业类别的关系
    @Relationship(type = "OPERATES_IN", direction = Relationship.Direction.OUTGOING)
    private List<IndustryCategory> industries;
}
