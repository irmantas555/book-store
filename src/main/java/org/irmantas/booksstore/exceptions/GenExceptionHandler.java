package org.irmantas.booksstore.exceptions;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

@Component
@Order(-2)
public class GenExceptionHandler extends AbstractErrorWebExceptionHandler {

    @Autowired
    ApiErrors apiErrors;

    public GenExceptionHandler(ErrorAttributes errorAttributes,
                               WebProperties.Resources resources,
                               ApplicationContext applicationContext,
                               ServerCodecConfigurer configurer) {
        super(errorAttributes, resources, applicationContext);
        this.setMessageWriters(configurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::modErrorResponse);

    }

    private Mono<ServerResponse> modErrorResponse(ServerRequest request) {
        String query = request.uri().getQuery();
        ErrorAttributeOptions options = isStackTraceEnabled(query) ?
                ErrorAttributeOptions.of(ErrorAttributeOptions.Include.STACK_TRACE) :
                ErrorAttributeOptions.defaults();
        Map<String, Object> myErrorAttributes = getErrorAttributes(request, options);
        apiErrors.setMessage(null);
        int intstatus = (int)Optional.ofNullable(myErrorAttributes.get("status")).orElse(500);
        return ServerResponse
                .status(intstatus)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(myErrorAttributes));
    }

    private boolean isStackTraceEnabled(String query) {
        return !StringUtils.isEmpty(query) && query.contains("trace=true");
    }
}
