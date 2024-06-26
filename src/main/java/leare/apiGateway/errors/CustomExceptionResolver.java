package leare.apiGateway.errors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.Map;

import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;

import com.fasterxml.jackson.databind.ObjectMapper;

// Importa las clases necesarias

@Component
public class CustomExceptionResolver extends DataFetcherExceptionResolverAdapter {

    private final ObjectMapper objectMapper;

    public CustomExceptionResolver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected GraphQLError resolveToSingleError(Throwable throwable, DataFetchingEnvironment env) {

        System.out.println("-------------------->" +throwable.getClass());

        // Si la excepción es de tipo WebClientResponseException (Error al consumir el microservicio)
        if (throwable instanceof WebClientResponseException) {
            WebClientResponseException ex = (WebClientResponseException) throwable;

            // Crear un objeto de error personalizado para GraphQL (statusCode y responseBody)
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("uri", ex.getRequest().getURI());
            errorMap.put("statusCode", ex.getStatusCode().value());
            errorMap.put("responseBody", ex.getResponseBodyAsString());

            try {
                String errorMessage = objectMapper.writeValueAsString(errorMap);

                return GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.ExecutionAborted)
                    .message(errorMessage)
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .build();

            } catch (JsonProcessingException e) {
                return super.resolveToSingleError(throwable, env);
            }
        }
        if (throwable instanceof AuthError) {
            return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.ExecutionAborted)
                .message(throwable.getMessage())
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
        }
        return super.resolveToSingleError(throwable, env);
    }
}

