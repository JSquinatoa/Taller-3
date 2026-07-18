package server;


import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.stereotype.Service;

@Service
public class MyServiceTools {

    @McpTool(description = "Add two numeric values")
    public String sumar (
            @McpToolParam(description = "First Value") float x,
            @McpToolParam(description = "Second Value") float y
    ){
        // Ahora sí realizará la suma matemática antes de convertir el resultado a String
        return String.valueOf(x + y);
    }

}