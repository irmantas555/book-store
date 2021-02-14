package org.irmantas.booksstore.exceptions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Component
public class CustomAttributes extends DefaultErrorAttributes {
    @Autowired
    ApiErrors apiErrors;

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(request, options);
        Throwable throwable = getError(request);
        if (throwable instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) throwable;
            if (apiErrors.getMessage() != null) {
                errorAttributes.put("message", apiErrors.getMessage());
            } else {
                errorAttributes.put("message", rse.getMessage());
            }
            return errorAttributes;
        }
        return errorAttributes;
    }


}
