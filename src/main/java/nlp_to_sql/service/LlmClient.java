package nlp_to_sql.service;

public interface LlmClient {
    String complete(String systemPrompt, String message);
}
