package com.taller3.mcp.server;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class MyServiceTools {

    @Tool(description = "Add Tho numeric Values")
    public String sumar (
            @McpToolParam(description = "First Value") String x,
            @McpToolParam(description = "Second Value") String y
    ){
        return String.valueOf(x + y);
    }


}
