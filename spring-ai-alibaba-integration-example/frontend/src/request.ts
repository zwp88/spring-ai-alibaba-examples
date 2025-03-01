const BASE_URL = "/api/v1";

const decoder = new TextDecoder("utf-8");

export const getModels = async () => {
  const res = (await fetch(BASE_URL + "/dashscope/getModels", {
    method: "GET",
    headers: {
      "Content-Type": "application/json"
    }
  })) as any;

  const reader = res.body.getReader();

  let bufffer = "";
  await reader.read().then(function process({ done, value }) {
    if (done) return;

    bufffer += decoder.decode(value);

    return reader.read().then(process);
  });

  return JSON.parse(bufffer).data;
};

export const getChat = async (
  callback: (value: Uint8Array) => void,
  message?: string,
  model?: string,
  chatId?: string
) => {
  const res = (await fetch(BASE_URL + "/chat?prompt=" + message, {
    method: "GET",
    headers: {
      model: model ? model : "",
      chatId: chatId ? chatId : ""
    }
  })) as any;
  const reader = res.body.getReader();
  await reader.read().then(function process({ done, value }) {
    if (done) return;

    callback(value);

    return reader.read().then(process);
  });
};
