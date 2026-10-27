package nlp_to_sql.exception;

public class LlmTimeoutException extends RuntimeException{
    public LlmTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
