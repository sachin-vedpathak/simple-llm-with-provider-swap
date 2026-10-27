package nlp_to_sql.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm")
public record LlmProperties(String provider, AnthropicProps anthropic, OllamaProps ollama) {
    public record AnthropicProps(String apiKey, String model, String baseUrl) {}
    public record OllamaProps(String model, String baseUrl) {}
}
