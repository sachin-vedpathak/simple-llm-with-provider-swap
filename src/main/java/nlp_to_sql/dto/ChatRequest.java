package nlp_to_sql.dto;


import jakarta.validation.constraints.NotBlank;

public record ChatRequest (@NotBlank String message) {
}
