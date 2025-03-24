import { act, useEffect, useReducer, useState } from "react";
import createChartStreamConnection from "@/app/services/streamService";
import { Conversation, StreamParams, StreamAction } from "@/types/streamTypes";
import { nanoid } from "nanoid";

type UseMultiModelStreamState = {
	conversations: Map<string, Conversation>;
	error: Error | null;
};

const streamReducer = (
	state: UseMultiModelStreamState,
	action: StreamAction
): UseMultiModelStreamState => {
	switch (action.type) {
		case "REQUEST_START": {
			const conversation = state.conversations.get(action.conversationId) || {
				messages: [],
				activeRequests: new Map(),
			};

			return {
				...state,
				conversations: new Map(state.conversations).set(action.conversationId, {
					...conversation,
					activeRequests: new Map(conversation.activeRequests).set(
						action.requestId,
						action.model
					),
				}),
			};
		}
		case "MESSAGE_UPDATE": {
			const conversation = state.conversations.get(action.conversationId);
			if (!conversation) return state;

			const existingMessage = conversation.messages.find(
				(msg) =>
					msg.requestId === action.requestId && msg.model === action.model
			);

			const newMessages = existingMessage
				? conversation.messages.map((msg) => {
						return msg.requestId === action.requestId &&
							msg.model === action.model
							? {
									...msg,
									content: msg.content + action.content,
								}
							: msg;
					})
				: [
						...conversation.messages,
						{
							requestId: action.requestId,
							model: action.model,
							content: action.content,
							timeStamp: Date.now().toString(),
							startTime: Date.now().toString(),
						},
					];

			return {
				...state,
				conversations: new Map(state.conversations).set(action.conversationId, {
					...conversation,
					messages: newMessages,
				}),
			};
		}

		case "REQUEST_END": {
			const conversation = state.conversations.get(action.conversationId);
			if (!conversation) return state;

			const newRequests = new Map(conversation.activeRequests);
			newRequests.delete(action.requestId);

			return {
				...state,
				conversations: new Map(state.conversations).set(action.conversationId, {
					...conversation,
					activeRequests: newRequests,
				}),
			};
		}

		case "ERROR": {
			return {
				...state,
				error: action.error,
			};
		}

		default:
			return state;
	}
};

const useMultiModelStream = (
	conversationId: string,
	streamParams: StreamParams
) => {
	const [state, dispatch] = useReducer(streamReducer, {
		conversations: new Map(),
		error: null,
	});

	useEffect(() => {
		if (!streamParams.prompt || !streamParams.conversationId) return;

		const requestId = nanoid();
		const requestTime = Date.now().toString();

		const cleanup = createChartStreamConnection(streamParams, {
			onMessage: (model, content) => {
				dispatch({
					type: "MESSAGE_UPDATE",
					conversationId,
					requestId,
					model,
					content: content,
				});
			},
			onError: (error) => {
				dispatch({
					type: "ERROR",
					error: error instanceof Error ? error : new Error("Stream failed"),
				});
			},
			onOpen: () => {
				dispatch({
					type: "REQUEST_START",
					conversationId,
					requestId,
					requestTime,
					model: streamParams.model || [],
				});
			},
			onClose: () => {
				dispatch({
					type: "REQUEST_END",
					conversationId,
					requestId,
				});
			},
		});

		return () => {
			cleanup();
		};
	}, [streamParams.prompt, streamParams.conversationId]);

	return {
		...state,
		getConversationsState: (conversationId: string) => {
			return {
				messages: state.conversations.get(conversationId)?.messages || [],
				isLoading:
					!!state.conversations.get(conversationId)?.activeRequests.size,
			};
		},
		error: state.error,
	};
};

export default useMultiModelStream;
