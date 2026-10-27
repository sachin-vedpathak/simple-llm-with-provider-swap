package nlp_to_sql.service;

import nlp_to_sql.config.LlmProperties;
import nlp_to_sql.exception.LlmProviderException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "llm.provider",havingValue = "ollama")
public class OllamaChatService implements LlmClient{

    private final RestClient restClient;
    private final LlmProperties llmProperties;

    public OllamaChatService(@Qualifier("ollamaRestClient") RestClient restClient, LlmProperties llmProperties) {
        this.restClient = restClient;
        this.llmProperties = llmProperties;
    }

    @Override
    public String complete(String systemPrompt, String message) {

        Map<String, Object> requestPayload = Map.of(
                "model", llmProperties.ollama().model(),
                "stream", false,
                "messages", List.of(
                        Map.of("role","system","content", systemPrompt),
                        Map.of("role","user", "content", message)
                )
        );

        OllamaResponse response = restClient.post()
                .uri("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestPayload)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,(req,res)->{
                    String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    throw new LlmProviderException(
                            "LLM Provider rejected the request " + res.getStatusCode() + " - " + body);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req,res)->{
                    String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    throw new LlmProviderException(
                            "LLM provider is currently unavailable: " + res.getStatusCode() + " - " + body);
                })
                .body(OllamaResponse.class);

        return response.message().content();
    }
    private record OllamaResponse(Message message) {
        private record Message(String role, String content) {}
    }
}
