package tmtd.event.common;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

  // Giữ nguyên nếu service/controller đã ném ResponseStatusException
  @ExceptionHandler(ResponseStatusException.class)
  public Map<String, Object> handleRse(ResponseStatusException ex) {
    throw ex;
  }

  // Tham số/validate sai → 400
  @ExceptionHandler({ IllegalArgumentException.class, MethodArgumentNotValidException.class })
  public Map<String, Object> handleBadRequest(Exception ex) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
  }

  // Lỗi nghiệp vụ (ví dụ “Event is full”, “Already registered”) → 409
  @ExceptionHandler(IllegalStateException.class)
  public Map<String, Object> handleConflict(IllegalStateException ex) {
    throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
  }

  // Vi phạm unique key (đăng ký trùng) → 409
  @ExceptionHandler(DataIntegrityViolationException.class)
  public Map<String, Object> handleDup(DataIntegrityViolationException ex) {
    throw new ResponseStatusException(HttpStatus.CONFLICT, "Already registered", ex);
  }
}
