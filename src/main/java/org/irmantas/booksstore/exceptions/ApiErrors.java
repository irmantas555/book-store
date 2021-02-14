package org.irmantas.booksstore.exceptions;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Data
@Component
@NoArgsConstructor
public class ApiErrors {

    private HttpStatus status;
    private String message;
    private List<String> errors;

    public ApiErrors(HttpStatus status, String message, List<String> errors) {
        super();
        this.status = status;
        this.message = message;
        this.errors = errors;
    }


    public ApiErrors(HttpStatus status, String message) {
        super();
        this.status = status;
        this.message = message;
        this.errors = errors;
    }

    public ApiErrors(HttpStatus status, String message, String error) {
        super();
        this.status = status;
        this.message = message;
        errors = Arrays.asList(error);
    }

    public void apiErrorsReset(){
        status = null;
        message = null;
        errors = null;
    }
}