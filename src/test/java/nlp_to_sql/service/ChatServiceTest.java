package nlp_to_sql.service;

import nlp_to_sql.config.LlmProperties;
import nlp_to_sql.exception.LlmProviderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

public class ChatServiceTest {

    private static final String ANTHROPIC_URL = "https://api.anthropic.com/v1/messages";

    private MockRestServiceServer mockServer;
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.anthropic.com");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        LlmProperties llmProperties =
                new LlmProperties("fake-key",
                        new LlmProperties.AnthropicProps("fake-key", "claude-3-5-sonnet-latest", "https://api.anthropic.com"),
                        new LlmProperties.OllamaProps("qwen2.5:3b", "http://localhost:11434"));
        chatService = new ChatService(restClient, llmProperties);
    }

    @Test
    void complete_returnsExtractedText_onSuccess() {
        mockServer.expect(requestTo(ANTHROPIC_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"content": [{"type": "text", "text": "Hello from LLM"}]}
                        """, MediaType.APPLICATION_JSON));

        String result = chatService.complete("system prompt", "hi");

        assertThat(result).isEqualTo("Hello from LLM");
        mockServer.verify();
    }

    @Test
    void complete_throwsLlmProviderException_on400() {
        mockServer.expect(requestTo(ANTHROPIC_URL))
                .andRespond(withBadRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {"type":"error","error":{"type":"invalid_request_error","message":"bad request"}}
                                """));

        assertThatThrownBy(() -> chatService.complete("sys", "msg"))
                .isInstanceOf(LlmProviderException.class)
                .hasMessageContaining("400");

        mockServer.verify();
    }

    @Test
    void complete_throwsLlmProviderException_on500() {
        mockServer.expect(requestTo(ANTHROPIC_URL)).andRespond(withServerError());

        assertThatThrownBy(() -> chatService.complete("sys", "msg"))
                .isInstanceOf(LlmProviderException.class);

        mockServer.verify();
    }
}
