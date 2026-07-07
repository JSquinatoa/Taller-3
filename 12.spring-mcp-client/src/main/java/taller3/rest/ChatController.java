package taller3.rest;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder builder, ToolCallbackProvider tools){
        this.chatClient = builder
                .defaultAdvisors(
                        // Esto es para imprimri el log de peticiones
                        new SimpleLoggerAdvisor()
                )
                .defaultTools(tools)
                .build();
    }

    @PostMapping(path = "/api/chat", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@RequestBody ChatRequest request) {

        var message = request.message();

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacío");
        }


        Flux<ServerSentEvent<String>> tokens = chatClient.prompt()
                .user(message)
//                .advisors(qaAdvisor)
                .stream()
                .content()
                .map(chunk -> ServerSentEvent.<String>builder()
                        .event("token")
                        .data(
                                Base64.getEncoder().encodeToString(chunk.getBytes(StandardCharsets.UTF_8))
                        )
                        .build()


                );

        Flux<ServerSentEvent<String>> done = Flux.just(
                ServerSentEvent.<String>builder()
                        .event("done")
                        .data("[DONE]")
                        .build()
        );

        return tokens.concatWith(done)
                .onErrorResume(error -> Flux.just(
                                ServerSentEvent.<String>builder()
                                        .event("error")
                                        .data(error.getMessage())
                                        .build()

                        )
                );

    }

}
