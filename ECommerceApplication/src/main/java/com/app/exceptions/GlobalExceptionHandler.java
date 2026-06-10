package com.app.exceptions;

import com.app.payloads.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	private ErrorResponse buildResponse(
			HttpStatus status,
			String message,
			HttpServletRequest request
	) {

		return ErrorResponse.builder()
				.timestamp(LocalDateTime.now())
				.status(status.value())
				.error(status.getReasonPhrase())
				.message(message)
				.path(request.getRequestURI())
				.build();
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFound(
			ResourceNotFoundException ex,
			HttpServletRequest request
	) {

		log.warn("Resource not found: {}", ex.getMessage());

		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(buildResponse(
						HttpStatus.NOT_FOUND,
						ex.getMessage(),
						request
				));
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleUserNotFound(
			UserNotFoundException ex,
			HttpServletRequest request
	) {

		log.warn("User not found: {}", ex.getMessage());

		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(buildResponse(
						HttpStatus.NOT_FOUND,
						ex.getMessage(),
						request
				));
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials(
			BadCredentialsException ex,
			HttpServletRequest request
	) {

		ErrorResponse error = ErrorResponse.builder()
				.timestamp(LocalDateTime.now())
				.status(401)
				.error("Unauthorized")
				.message("Invalid email or password")
				.path(request.getRequestURI())
				.build();

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(error);
	}

	@ExceptionHandler(APIException.class)
	public ResponseEntity<ErrorResponse> handleApiException(
			APIException ex,
			HttpServletRequest request
	) {

		log.warn("API Exception: {}", ex.getMessage());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(buildResponse(
						HttpStatus.BAD_REQUEST,
						ex.getMessage(),
						request
				));
	}

	@ExceptionHandler(TokenExpiredException.class)
	public ResponseEntity<ErrorResponse> handleTokenExpired(
			TokenExpiredException ex,
			HttpServletRequest request
	) {

		ErrorResponse error = ErrorResponse.builder()
				.timestamp(LocalDateTime.now())
				.status(400)
				.error("BAD_REQUEST")
				.message(ex.getMessage())
				.path(request.getRequestURI())
				.build();

		return ResponseEntity.badRequest().body(error);
	}

	@ExceptionHandler(EmailVerificationException.class)
	public ResponseEntity<ErrorResponse> handleEmailVerificationException(
			EmailVerificationException ex,
			HttpServletRequest request
	) {

		log.warn("Email verification failed: {}", ex.getMessage());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(buildResponse(
						HttpStatus.BAD_REQUEST,
						ex.getMessage(),
						request
				));
	}

	@ExceptionHandler(EmailNotVerifiedException.class)
	public ResponseEntity<ErrorResponse> handleEmailNotVerified(
			EmailNotVerifiedException ex,
			HttpServletRequest request
	) {

		log.warn("Email not verified: {}", ex.getMessage());

		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(buildResponse(
						HttpStatus.FORBIDDEN,
						ex.getMessage(),
						request
				));
	}

	@ExceptionHandler(FileStorageException.class)
	public ResponseEntity<ErrorResponse> handleFileStorageException(
			FileStorageException ex,
			HttpServletRequest request
	) {

		log.error("File storage error", ex);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(buildResponse(
						HttpStatus.INTERNAL_SERVER_ERROR,
						ex.getMessage(),
						request
				));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDenied(
			AccessDeniedException ex,
			HttpServletRequest request
	) {

		log.warn("Access denied: {}", request.getRequestURI());

		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(buildResponse(
						HttpStatus.FORBIDDEN,
						"Access Denied",
						request
				));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(
			MethodArgumentNotValidException ex,
			HttpServletRequest request
	) {

		String errors = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(FieldError::getDefaultMessage)
				.collect(Collectors.joining(", "));

		log.warn("Validation failed: {}", errors);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(buildResponse(
						HttpStatus.BAD_REQUEST,
						errors,
						request
				));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDatabaseException(
			DataIntegrityViolationException ex,
			HttpServletRequest request
	) {

		log.error("Database constraint violation", ex);

		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(buildResponse(
						HttpStatus.CONFLICT,
						"Database constraint violation",
						request
				));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(
			Exception ex,
			HttpServletRequest request
	) {

		log.error("Unhandled exception", ex);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(buildResponse(
						HttpStatus.INTERNAL_SERVER_ERROR,
						"An unexpected error occurred",
						request
				));
	}


	@ExceptionHandler(
			ObjectOptimisticLockingFailureException.class
	)
	public ResponseEntity<ErrorResponse>
	handleOptimisticLocking(
			ObjectOptimisticLockingFailureException ex
	) {

		ErrorResponse response =
				ErrorResponse.builder()
						.status(HttpStatus.CONFLICT.value())
						.error("Conflict")
						.message(
								"Data was modified by another user. Please retry."
						)
						.build();

		return ResponseEntity
				.status(HttpStatus.CONFLICT)
				.body(response);
	}
}
