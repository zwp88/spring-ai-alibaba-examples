package com.alibaba.cloud.ai.application.websearch.rag.prompt;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@Configuration
public class PromptTemplateConfig {

	@Bean
	public PromptTemplate transformerPromptTemplate() {

		return new PromptTemplate(
				"""
				Given a user query, rewrite it to provide better results when querying a {target}.
				Remove any irrelevant information, and ensure the query is concise and specific.
				
				The output must be consistent with the user query's language.
				
				Original query:
				{query}
				
				Rewritten query:
				"""
		);
	}


	@Bean
	public PromptTemplate queryArgumentPromptTemplate() {

		return new PromptTemplate(
				"""
				You will be given a set of related document contexts to the question.
				Each document starts with a reference number like [[x]], where x is a number that may be repeated.
				Document without reference will be marked as [[null]].
				Please use the context and cite the context at the end of each sentence if applicable.
				Context information is below.
				
				---------------------
				{context}
				---------------------
				
				Given the context information and no prior knowledge, generate a structured response to the user's question.
				
				Follow these rules:
				
				1. If the answer is not in the context, just say that you don't know.
				2. Do not give any information that is not related to the question, and do not repeat.
				3. Avoid statements like "Based on the context..." or "The provided information...".
				4. Your answer must be correct, accurate and written by an expert using an unbiased and professional tone.
				5. The appropriate structure is automatically determined based on the characteristics of the content, but be sure to include subheadings to enhance readability.
				6. When generating a response, provide a clear conclusion or main idea first, and do not need to carry a title.
				7. Make sure each section has clear subheadings so the reader can better understand and follow the content.
				8. If the information is complex or has multiple sections, make sure each section has an appropriate title to create a hierarchical structure.
				9. Please cite the sentence or section with the reference numbers in the end, in the format [[x]].
				10. If a sentence or sections comes from multiple contexts, please list all applicable citations, like [[x]][[y]].
				11. You don`t have to cite if the reference is marked as [[null]].
				12. Other than code and specific names and citations, your answer must be written in the same language as the question.

				Query: {query}
				
				Answer:
				"""
		);
	}

}
