package tracker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ValidationException;
import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class ToRpcConverter implements ResponseBodyAdvice<Object> {

    @Override public boolean supports(
        MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType
    ) {

        return returnType.getParameterType() != ResponseEntity.class &&
               returnType.getExecutable().getDeclaringClass().getName().startsWith("tracker.api");
    }

    record OkResponse(boolean isOk, Object value) { }

    @Override public Object beforeBodyWrite(
        Object body, MethodParameter returnType,
        MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType,
        ServerHttpRequest request, ServerHttpResponse response
    ) {
        return new OkResponse(true, body);
    }

    record Error(String kind, String message) { }
    record ErrorResponse(boolean isOk, Error error) { }

    @ExceptionHandler(ValidationException.class)
    Object handleException(RuntimeException ex) throws JsonProcessingException {

        return new ResponseEntity<>(
            new ObjectMapper().writeValueAsString(
                new ErrorResponse(false, new Error(ex.getClass().getName(), ex.getMessage()))
            ),
            HttpStatusCode.valueOf(200)
        );
    }
}
