export type Model = "ollama" | "dashScope" | "user";

export type StreamMessage = {
	model: Model;
	content: string;
	requestId: string;
	startTime: string;
	timeStamp?: string;
	disableMdKit?: boolean;
};

export type StreamParams = {
	prompt: string;
	model?: Model[];
	conversationId: string;
	startTime?: string;
};

export type StreamHandlers = {
	onMessage: (model: Model, content: string) => void;
	onError: (error: Error) => void;
	onOpen?: () => void;
	onClose?: () => void;
};

export type StreamAction =
	| {
			type: "REQUEST_START";
			conversationId: string;
			requestId: string;
			content: string;
			requestTime: string;
			model: Model;
			models?: Model[];
	  }
	| {
			type: "MESSAGE_UPDATE";
			conversationId: string;
			requestId: string;
			model: Model;
			content: string;
	  }
	| {
			type: "REQUEST_END";
			conversationId: string;
			requestId: string;
	  }
	| {
			type: "ERROR";
			error: Error;
	  };

export type Conversation = {
	modelMessages: Map<Model, StreamMessage[]>;
	messages: StreamMessage[];
	activeRequests: Map<string, Model>;
};
