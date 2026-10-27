package nlp_to_sql.exception;

public class LlmProviderException extends RuntimeException{
    public LlmProviderException(String message) {
        super(message);
    }
}
