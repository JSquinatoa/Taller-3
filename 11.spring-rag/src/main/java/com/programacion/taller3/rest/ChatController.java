package com.programacion.taller3.rest;

import org.apache.camel.spi.RestConfiguration;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
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
    private final RestConfiguration configuration;

    @Value("classpath:/prompts/systemPrompt.st")
    Resource systemPrompt;

    @Autowired
    VectorStore vectorStore;


    public ChatController(ChatClient.Builder builder, RestConfiguration configuration){
        this.chatClient = builder
                .defaultAdvisors(
                        // Esto es para imprimri el log de peticiones
                        new SimpleLoggerAdvisor()
                )
                .build();
        this.configuration = configuration;

    }
    private  String searchDocuemnts (String query){
        var request = SearchRequest.builder()
                .query(query)
                .topK(3)
                .build();

        var documents = vectorStore.similaritySearch(request);

        return  documents.stream()
                .map(Document::getText)
                .reduce("", (a, b) -> a+ System.lineSeparator() + b)
                .trim();

    }

//    @PostMapping(value = "/chat", consumes = "application/json")
//    public String chat(@RequestBody ChatRequest request){
//
//        // convertir la pregunta a vector
//        //buscar en la base vecotiral
//        // poner el contexto ene l promp del sistema
//        String contexto = "";
//
//        return chatClient.prompt()
//                .system(systemSpec ->
//                        systemSpec
//                                .text(systemPrompt)
//                                .param("normativa", contexto)
//
//                )
//                .user(userSpec ->
//                        userSpec.text(request.message())
//                )
//                .call()
//                .content();
//
//    }

    @PostMapping(path = "/api/chat", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@RequestBody ChatRequest request) {

        var message = request.message();

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacío");
        }

//        // convertir la pregunta a vector
//        //buscar en la base vecotiral
//        // poner el contexto ene l promp del sistema
        String contexto = searchDocuemnts(message);

//        Flux<ServerSentEvent<String>> tokens = chatClient.prompt()
//                .system(systemSpec -> systemSpec
//                        .text(systemPrompt)
//                        .param("normativa", contexto )
//                )
//                .user(message)
//                .stream()
//                .content()
//                .map(chunk -> ServerSentEvent.<String>builder()
//                        .event("token")
//                        .data(
//                                Base64.getEncoder().encodeToString(chunk.getBytes(StandardCharsets.UTF_8))
//                        )
//                        .build()
//
//
//                );

        var qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(
                        SearchRequest.builder()
                                .query(message)
                                .topK(3)
                                .build()
                )
                .build();

        Flux<ServerSentEvent<String>> tokens = chatClient.prompt()
                .user(message)
                .advisors(qaAdvisor)
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
