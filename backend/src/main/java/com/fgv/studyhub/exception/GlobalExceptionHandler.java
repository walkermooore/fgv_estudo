package com.fgv.studyhub.exception;
import com.fgv.studyhub.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
 @ExceptionHandler({BadRequestException.class,ConstraintViolationException.class}) ResponseEntity<ErrorResponseDTO> bad(RuntimeException e,HttpServletRequest r){return response(HttpStatus.BAD_REQUEST,e.getMessage(),r,Map.of());}
 @ExceptionHandler(NotFoundException.class) ResponseEntity<ErrorResponseDTO> notFound(RuntimeException e,HttpServletRequest r){return response(HttpStatus.NOT_FOUND,e.getMessage(),r,Map.of());}
 @ExceptionHandler({PayloadTooLargeException.class,MaxUploadSizeExceededException.class}) ResponseEntity<ErrorResponseDTO> large(Exception e,HttpServletRequest r){return response(HttpStatus.PAYLOAD_TOO_LARGE,e.getMessage(),r,Map.of());}
 @ExceptionHandler(UnsupportedMediaException.class) ResponseEntity<ErrorResponseDTO> media(RuntimeException e,HttpServletRequest r){return response(HttpStatus.UNSUPPORTED_MEDIA_TYPE,e.getMessage(),r,Map.of());}
 @ExceptionHandler(AiRateLimitException.class) ResponseEntity<ErrorResponseDTO> rate(RuntimeException e,HttpServletRequest r){return response(HttpStatus.TOO_MANY_REQUESTS,e.getMessage(),r,Map.of());}
 @ExceptionHandler({AiTimeoutException.class}) ResponseEntity<ErrorResponseDTO> timeout(RuntimeException e,HttpServletRequest r){return response(HttpStatus.GATEWAY_TIMEOUT,e.getMessage(),r,Map.of());}
 @ExceptionHandler({AiParsingException.class,ExtractionException.class,AiServiceException.class}) ResponseEntity<ErrorResponseDTO> service(RuntimeException e,HttpServletRequest r){return response(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage(),r,Map.of());}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ErrorResponseDTO> validation(MethodArgumentNotValidException e,HttpServletRequest r){var errors=e.getBindingResult().getFieldErrors().stream().collect(Collectors.toMap(x->x.getField(),x->Optional.ofNullable(x.getDefaultMessage()).orElse("invalid"),(a,b)->a)); return response(HttpStatus.BAD_REQUEST,"Validation failed",r,errors);}
 @ExceptionHandler(Exception.class) ResponseEntity<ErrorResponseDTO> unknown(Exception e,HttpServletRequest r){return response(HttpStatus.INTERNAL_SERVER_ERROR,"An unexpected error occurred",r,Map.of());}
 private ResponseEntity<ErrorResponseDTO> response(HttpStatus status,String message,HttpServletRequest request,Map<String,String> errors){return ResponseEntity.status(status).body(new ErrorResponseDTO(Instant.now(),status.value(),status.getReasonPhrase(),message,request.getRequestURI(),errors));}
}
