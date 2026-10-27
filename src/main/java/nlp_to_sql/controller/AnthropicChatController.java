package nlp_to_sql.controller;

import jakarta.validation.Valid;
import nlp_to_sql.dto.ChatRequest;
import nlp_to_sql.dto.ChatResponse;
import nlp_to_sql.service.ChatService;
import nlp_to_sql.service.LlmClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AnthropicChatController {
    private static final String DEFAULT_SYSTEM_PROMPT = "You are a helpful assistant";
    ChatService chatService;
    LlmClient llmClient;

    public AnthropicChatController(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    @PostMapping("/chat")
    public ChatResponse complete(@Valid @RequestBody ChatRequest request) {
        String answer = llmClient.complete(DEFAULT_SYSTEM_PROMPT,request.message());
        return new ChatResponse(answer);
    }

}
