package com.alibaba.example.summarizer;

public class SQLGenerationException extends RuntimeException {

	public SQLGenerationException(String response) {
		super(response);
	}
}
