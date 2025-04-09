import { createStyles } from "antd-style";

export const useStyle = createStyles(({ token, css }) => {
  return {
    chat: css`
      height: 100%;
      /* width: calc(100% - 420px); */
      margin: 0 auto;
      box-sizing: border-box;
      display: flex;
      flex-direction: column;
      padding: ${token.paddingLG}px;
      padding-bottom: 0;
      margin: 0 8%;
      gap: 16px;
      transition: all 0.3s cubic-bezier(0.34, 0.69, 0.1, 1);
      position: relative;
    `,
    chatFullWidth: css`
      width: 100%;
    `,
    messages: css`
      flex: 1;
      margin-bottom: 16px;
      padding-bottom: 40px;
      overflow-y: auto;
    `,
    placeholder: css`
      padding-top: 32px;
    `,
    sender: css`
      box-shadow: ${token.boxShadow};
      margin-bottom: 50px;
      position: relative;
      border-radius: 12px;
      width: 100%;

      .ant-sender-textarea {
        transition: height 0.3s ease, max-height 0.3s ease;
        min-height: 56px;
      }
    `,
    senderExpanded: css`
      .ant-sender-textarea {
        height: 300px !important;
        max-height: 300px !important;
      }
    `,
    expandToggle: css`
      position: absolute;
      right: 10px;
      top: 10px;
      z-index: 10;
      opacity: 0.7;
      transition: opacity 0.2s;
      background-color: ${token.colorBgElevated};
      border-radius: 4px;
      width: 28px;
      height: 28px;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      cursor: pointer;

      &:hover {
        opacity: 1;
        background-color: ${token.colorBgContainer};
      }
    `,
    actionButtons: css`
      display: flex;
      gap: 8px;
      margin-bottom: 8px;
    `,
    actionButton: css`
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 2px 14px;
      border-radius: ${token.borderRadius}px;
      font-size: 14px;
      transition: all 0.2s;
      cursor: pointer;

      .anticon {
        font-size: 14px;
      }

      &:hover {
        background-color: ${token.colorPrimaryBg};
      }
    `,
    searchButton: css`
      color: #1677ff;
      border: 1px solid #1677ff30;
      background-color: #1677ff08;

      &:hover {
        background-color: #1677ff15;
      }

      &.active {
        outline: 2px solid #1677ff;
        outline-offset: -1px;
        border-color: transparent;
        background-color: #1677ff12;
      }
    `,
    thinkButton: css`
      color: #722ed1;
      border: 1px solid #722ed130;
      background-color: #722ed108;

      &:hover {
        background-color: #722ed115;
      }

      &.active {
        outline: 2px solid #722ed1;
        outline-offset: -1px;
        border-color: transparent;
        background-color: #722ed112;
      }
    `,
    activeButton: css`
      font-weight: 500;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
      transform: translateY(-1px);
    `,
    chatPageSender: css`
      position: fixed;
      bottom: 12px;
      padding: 0;
      margin-top: 2px;
    `,
    container: css`
      display: flex;
      flex-direction: column;
      margin: 40px 0 80px;
      height: calc(100vh - 224px);
      width: 100%;
      position: relative;
      overflow: hidden;
    `,
    messagesContainer: css`
      flex: 1;
      overflow-y: auto;
      padding: 16px;
      display: flex;
      flex-direction: column;
      gap: 16px;
      scroll-behavior: smooth;
      height: calc(100vh - 200px);
      &::-webkit-scrollbar {
        width: 6px;
      }
      &::-webkit-scrollbar-track {
        background: transparent;
      }
      &::-webkit-scrollbar-thumb {
        background: ${token.colorTextTertiary};
        border-radius: 3px;
      }
      &::-webkit-scrollbar-thumb:hover {
        background: ${token.colorTextSecondary};
      }
    `,
    senderContainer: css`
      position: fixed;
      bottom: 32px;
      left: 314px;
      width: calc(100% - 350px);
    `,
    senderContainerCollapsed: css`
      position: fixed;
      bottom: 32px;
      width: 100%;
      padding: 0 16px;
    `,
    landingContainer: css`
      display: flex;
      flex-direction: column;
      height: 100%;
      width: calc(100% - 12px);
    `,
    landingContent: css`
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      margin-bottom: 50px;
    `,
    landingSender: css`
      position: fixed;
      bottom: 80px;
      left: 324px;
      width: calc(100% - 360px);
    `,
    landingSenderCollapsed: css`
      position: fixed;
      bottom: 80px;
      left: 50%;
      transform: translate(-50%);
    `,

    botMessage: css`
      align-self: flex-start;
      max-width: 80%;
      background-color: ${token.colorBgElevated};
      padding: 12px 16px;
      border-radius: 12px 12px 12px 0;
      margin-bottom: 16px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    `,
    userMessage: css`
      align-self: flex-end;
      max-width: 80%;
      background-color: ${token.colorPrimary};
      color: white;
      padding: 12px 16px;
      border-radius: 12px 12px 0 12px;
      margin-bottom: 16px;
    `,
    messageSender: css`
      font-size: 12px;
      margin-bottom: 4px;
      font-weight: 500;
      color: ${token.colorTextSecondary};
    `,
    messageText: css`
      font-size: 14px;
      line-height: 1.5;
      white-space: pre-wrap;
      word-break: break-word;
    `,
    messageTime: css`
      font-size: 10px;
      color: ${token.colorTextQuaternary};
      margin-top: 4px;
      text-align: right;
    `,
  };
});
