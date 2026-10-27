package nlp_to_sql.service;

import nlp_to_sql.config.LlmProperties;
import nlp_to_sql.exception.LlmProviderException;
import nlp_to_sql.exception.LlmTimeoutException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "llm.provider",havingValue = "anthropic",matchIfMissing = true)
public class ChatService implements LlmClient {
    final RestClient restClient;
    private final LlmProperties llmProperties;

    public ChatService(RestClient restClient, LlmProperties llmProperties) {
        this.restClient = restClient;
        this.llmProperties = llmProperties;
    }

    @Override
    public String complete(String systemPrompt, String message) {

        Map<String, Object> requestPayload = Map.of(
                "max_tokens", 1024,
                "model", llmProperties.anthropic().model(),
                "system", systemPrompt,
                "messages", List.of(
                        Map.of("role", "user", "content", message)
                )
        );

        try {
            AnthropicResponse response = restClient.post()
                    .uri("/v1/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        throw new LlmProviderException(
                                "LLM provider rejected the request: " + res.getStatusCode() + " - " + body);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        throw new LlmProviderException(
                                "LLM provider is currently unavailable: " + res.getStatusCode() + " - " + body);
                    })
                    .body(AnthropicResponse.class);
            return response.content().get(0).text();

        } catch (ResourceAccessException ex) {
            throw new LlmTimeoutException("LLM provider call timed out", ex);
        }
    }
    // Anthropic-specific response shape — stays private to this class,
    // never leaks outside ChatService.
    private record AnthropicResponse(List<ContentBlock> content) {
        private record ContentBlock(String type, String text) {}
    }
}
