/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.ai.application.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.alibaba.cloud.ai.vectorstore.opensearch.OpenSearchVectorStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@Component
public class LocalRAGVectorStoreInit implements ApplicationRunner {

	private final Logger logger = LoggerFactory.getLogger(LocalRAGVectorStoreInit.class);

	private final OpenSearchVectorStore vectorStore;

	private final List<MarkdownDocumentReader> markdownDocumentReaderList;

	public LocalRAGVectorStoreInit(OpenSearchVectorStore vectorStore) throws IOException {

		this.vectorStore = vectorStore;
		this.markdownDocumentReaderList = loadMarkdownDocuments();
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {

		int size = 0;
		if (markdownDocumentReaderList.isEmpty()) {
			logger.warn("No markdown documents found in the directory.");
			return;
		}

		logger.debug("Start to load markdown documents into vector store......");
		for (MarkdownDocumentReader markdownDocumentReader : markdownDocumentReaderList) {
			vectorStore.add(markdownDocumentReader.get());
			size += markdownDocumentReader.get().size();
		}
		logger.debug("Load markdown documents into vector store successfully. Load {} documents.", size);
	}

	private List<MarkdownDocumentReader> loadMarkdownDocuments() throws IOException {

		List<MarkdownDocumentReader> readers = new ArrayList<>();
		Path markdownDir = Paths.get(getClass().getClassLoader().getResource("rag/markdown").getPath());

		try (Stream<Path> paths = Files.walk(markdownDir)) {

			readers = paths.filter(Files::isRegularFile)
					.filter(path -> path.toString().endsWith(".md"))
					.map(path -> new MarkdownDocumentReader(path.toFile().getName()))
					.collect(Collectors.toList());
		}

		return readers;
	}

}
