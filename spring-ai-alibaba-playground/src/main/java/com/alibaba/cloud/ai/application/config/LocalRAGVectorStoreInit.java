package com.alibaba.cloud.ai.application.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
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

	private final MilvusVectorStore vectorStore;

	private final List<MarkdownDocumentReader> markdownDocumentReaderList;

	public LocalRAGVectorStoreInit(MilvusVectorStore vectorStore) throws IOException {

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
