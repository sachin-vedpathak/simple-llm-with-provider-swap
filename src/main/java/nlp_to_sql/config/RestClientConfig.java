package nlp_to_sql.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    @Qualifier("anthropicRestClient")
    public RestClient restClient(RestClient.Builder builder, LlmProperties llmProperties) {
        HttpClientSettings settings = HttpClientSettings.defaults()
                .withConnectTimeout(Duration.ofSeconds(5))
                .withReadTimeout(Duration.ofSeconds(30));

        ClientHttpRequestFactory factory = ClientHttpRequestFactoryBuilder.detect().build(settings);


        return builder
                .baseUrl(llmProperties.anthropic().baseUrl())
                .requestFactory(factory)
                .defaultHeader("Accept", "application/json")
                .defaultHeader("x-api-key",llmProperties.anthropic().apiKey())
                .defaultHeader("anthropic-version", "2026-10-21")
                .build();
    }

    @Bean
    @Qualifier("ollamaRestClient")
    public RestClient ollamaRestClient(RestClient.Builder builder, LlmProperties props) {
        return builder
                .baseUrl(props.ollama().baseUrl())
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
