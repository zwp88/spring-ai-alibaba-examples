"use client"; // [!code focus]
import {
	Attachments,
	Bubble,
	Conversations,
	Prompts,
	Sender,
	useXAgent,
	useXChat,
} from "@ant-design/x";
import React, { useEffect } from "react";

import { PaperClipOutlined, PlusOutlined } from "@ant-design/icons";
import { Badge, Button, type GetProp, Layout, Tooltip } from "antd";
import { PlaceHolderNode, LogoNode, FriendlyLinkBar } from "./components";
import { useQueryClient } from "@tanstack/react-query";

const { Header, Sider, Content } = Layout;

const defaultConversationsItems = [
	{
		key: "0",
		label: "Conversation",
	},
];

const roles: GetProp<typeof Bubble.List, "roles"> = {
	ai: {
		placement: "start",
		typing: { step: 5, interval: 20 },
		styles: {
			content: {
				borderRadius: 16,
			},
		},
	},
	local: {
		placement: "end",
		variant: "shadow",
	},
};

const Independent: React.FC = () => {
	// ==================== State ====================
	const [sideCollapsed, setSideCollapsed] = React.useState(false);
	const [headerOpen, setHeaderOpen] = React.useState(false);
	const [content, setContent] = React.useState("");
	const [conversationsItems, setConversationsItems] = React.useState(
		defaultConversationsItems
	);
	const queryClient = useQueryClient();
	const [activeKey, setActiveKey] = React.useState(
		defaultConversationsItems[0].key
	);
	const [attachedFiles, setAttachedFiles] = React.useState<
		GetProp<typeof Attachments, "items">
	>([]);

	//  ==================== EventsourceQuery ====================
	const [eventSourceAgent] = useXAgent({
		request: async ({ message }, { onSuccess, onError }) => {
			const queryKey = ["chartStream", activeKey, message];

			// react-query 管理响应

			await queryClient.fetchQuery({
				queryKey,
				queryFn: async () => {
					const eventSource = new EventSource(
						`/api/stream/chat?prompt=${encodeURIComponent(message as string)}&conversationId=${activeKey}`,
						{
							withCredentials: true,
							// 添加自定义头
						}
					);

					const messageMap = new Map<string, string>();

					return new Promise((resolve, reject) => {
						eventSource.onmessage = (event) => {
							console.log("event====>", event);
							// const data = JSON.parse(event.data);

							// const { event: eventType, content } = data;

							// if (["ollama", "dashScope"].includes(eventType)) {
							// 	if (!messageMap.has(eventType)) {
							// 		const messageId = crypto.randomUUID();
							// 		messageMap.set(eventType, messageId);
							// 		console.log(
							// 			"%c [ messageId, content,eventType ]: ",
							// 			"color: #bf2c9f; background: pink; font-size: 13px;",
							// 			"messageId, content,eventType"
							// 		);
							// 		// onMessage({
							// 		// 	id: messageId,
							// 		// 	message: content,
							// 		// 	status: "loading",
							// 		// 	role: "ai",
							// 		// 	model: eventType,
							// 		// });
							// 	} else {
							// 		const messageId = messageMap.get(eventType);
							// 		console.log(
							// 			"%c [ messageId, content, eventType ]: ",
							// 			"color: #bf2c9f; background: pink; font-size: 13px;",
							// 			"messageId, content, eventType"
							// 		);
							// 	}
							// }

							resolve(event);
						};

						eventSource.onerror = (error) => {
							console.error("SSE 错误详情:", {
								readyState: eventSource.readyState,
								url: eventSource.url,
								error: error,
							});
							eventSource.close();
							reject(error);
						};
						// 添加详细事件监听
						eventSource.addEventListener("open", () => {
							console.log("SSE 连接成功");
						});

						eventSource.addEventListener("done", () => {
							eventSource.close();
							messageMap.forEach((id) => {
								console.log(
									"%c [ id,  ]: ",
									"color: #bf2c9f; background: pink; font-size: 13px;",
									"id, "
								);

								resolve(null);
							});
						});
					});
				},
			});
		},
	});

	const { onRequest, messages, setMessages } = useXChat({
		agent: eventSourceAgent,
	});

	useEffect(() => {
		if (activeKey !== undefined) {
			setMessages([]);
		}
	}, [activeKey]);

	// ==================== Event ====================
	const onSubmit = (nextContent: string) => {
		if (!nextContent) return;
		onRequest(nextContent);
		setContent("");
	};

	const onPromptsItemClick: GetProp<typeof Prompts, "onItemClick"> = (info) => {
		onRequest(info.data.description as string);
	};

	const onAddConversation = () => {
		setConversationsItems([
			...conversationsItems,
			{
				key: `${conversationsItems.length}`,
				label: `New Conversation ${conversationsItems.length}`,
			},
		]);
		setActiveKey(`${conversationsItems.length}`);
	};

	const onConversationClick: GetProp<typeof Conversations, "onActiveChange"> = (
		key
	) => {
		setActiveKey(key);
	};

	const items: GetProp<typeof Bubble.List, "items"> = messages.map(
		({ id, message, status }) => ({
			key: id,
			loading: status === "loading",
			role: status === "local" ? "local" : "ai",
			content: message,
		})
	);

	const attachmentsNode = (
		<Badge dot={attachedFiles.length > 0 && !headerOpen}>
			<Tooltip title="暂不支持">
				<Button
					type="text"
					disabled
					icon={<PaperClipOutlined />}
					onClick={() => setHeaderOpen(!headerOpen)}
				/>
			</Tooltip>
		</Badge>
	);

	// ==================== Render =================
	return (
		<div className="w-full min-w-[1000px] h-screen min-h-[722px] rounded flex bg-white font-[AlibabaPuHuiTi,system-ui]">
			<Sider
				style={{
					background: "#fafafa",
				}}
				width={280}
				theme="dark"
				collapsedWidth="120"
				collapsible
				onBreakpoint={(broken) => {
					console.log(broken);
				}}
				onCollapse={(collapsed, type) => {
					setSideCollapsed(collapsed);
					console.log(collapsed, type);
				}}
				collapsed={sideCollapsed}>
				{/* 🌟 Logo */}
				<LogoNode showTitle={!sideCollapsed} />
				{/* 🌟 添加会话 */}
				<Button
					onClick={onAddConversation}
					style={{
						border: "1px solid #1677ff34",
						width: "calc(100% - 24px)",
						overflow: "hidden",
						textOverflow: "ellipsis",
						whiteSpace: "nowrap",
					}}
					icon={<PlusOutlined />}
					className={` text-ellipsis bg-[#1677ff0f]  border border-[#1677ff0f] mx-3 mb-6`}>
					{
						<span className="text-ellipsis w-[100%] overflow-hidden whitespace-nowrap">
							{sideCollapsed ? "New" : "Add Conversation"}
						</span>
					}
				</Button>
				{/* 🌟 会话管理 */}
				<Conversations
					items={conversationsItems}
					className="px-3 flex-1 overflow-y-auto"
					activeKey={activeKey}
					onActiveChange={onConversationClick}
				/>
			</Sider>

			<div className="h-full w-full max-w-[700px] mx-auto box-border flex flex-col p-2 gap-4">
				<FriendlyLinkBar />
				{/* 🌟 消息列表 */}
				<Bubble.List
					items={
						items.length > 0
							? items
							: [
									{
										content: (
											<PlaceHolderNode
												onPromptsItemClick={onPromptsItemClick}
											/>
										),
										variant: "borderless",
									},
								]
					}
					roles={roles}
					className="flex-1"
				/>
				{/* 🌟 输入框 */}
				<Sender
					value={content}
					onSubmit={onSubmit}
					onChange={setContent}
					prefix={attachmentsNode}
					loading={eventSourceAgent.isRequesting()}
					className="shadow-md"
				/>
			</div>
		</div>
	);
};

export default Independent;
