import { BASE_URL } from "../const";

// Define the structure for a single MCP Server based on backend response
export interface McpServer {
  id: string;
  name: string;
  description: string;
  // Add other relevant fields if available from the API
}

// Define the structure for the response of the list endpoint
interface McpListResponse {
  data: McpServer[];
  code: number;
  message?: string;
}

// Define the structure for the response of the run endpoint
interface McpRunResponse {
  data: any; // The data can be complex, handle appropriately
  code: number;
  message?: string;
}

// Function to fetch the list of MCP servers
export const getMcpList = async (): Promise<McpListResponse> => {
  try {
    const res = await fetch(`${BASE_URL}/mcp-list`, {
      method: "GET",
    });

    if (!res.ok) {
      throw new Error(`API request failed with status ${res.status}`);
    }

    const data = await res.json();
    return data as McpListResponse;
  } catch (error) {
    console.error("MCP List API call error:", error);
    // Return a standard error format or rethrow
    return {
      code: -1, // Use a specific code for client-side errors
      data: [],
      message: error instanceof Error ? error.message : "Unknown error",
    };
  }
};

// Renamed and updated function to run a specific MCP server
export const runMcp = async (
  id: string, // Server ID
  prompt: string,
  envs?: string
): Promise<McpRunResponse> => {
  try {
    // Corrected endpoint to match the backend controller
    const res = await fetch(`${BASE_URL}/mcp-run`, {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: new URLSearchParams({
        id: id,
        prompt: prompt,
        ...(envs ? { envs } : {}),
      }),
    });

    if (!res.ok) {
      // Attempt to read error message from response body
      let errorBody = "Unknown error";
      try {
        const errorData = await res.json();
        errorBody = errorData.message || JSON.stringify(errorData);
      } catch (e) {
        errorBody = await res.text();
      }
      throw new Error(
        `API request failed with status ${res.status}: ${errorBody}`
      );
    }

    const text = await res.text();
    let parsedData;

    try {
      parsedData = JSON.parse(text);
    } catch (e) {
      // If response is plain text
      return {
        code: 0, // Assuming success if response is plain text
        data: text,
      };
    }

    // Check if the parsed data has the expected structure (data, code, message)
    if (
      parsedData &&
      typeof parsedData === "object" &&
      parsedData.code !== undefined
    ) {
      return parsedData as McpRunResponse;
    } else {
      // If it's some other JSON structure, wrap it in the data field
      return {
        code: 0, // Assuming success
        data: parsedData,
      };
    }
  } catch (error) {
    console.error("MCP Run API call error:", error);
    // Return a standard error format
    return {
      code: -1, // Use a specific code for client-side errors
      data: null,
      message: error instanceof Error ? error.message : "Unknown error",
    };
  }
};
