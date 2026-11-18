package com.back.global.globalExceptionHandler;

import com.back.global.rsData.RsData;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * 컨트롤러에서 발생한 예외를 RsData 형식의 응답으로 바꿔준다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 존재하지 않는 글/댓글 조회 → 404
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<RsData<Void>> handle(NoSuchElementException ex) {
        return ResponseEntity
                .status(NOT_FOUND)
                .body(new RsData<>("404-1", "해당 데이터가 존재하지 않습니다."));
    }

    // @Valid 검증 실패 → 400, 메시지 형식 : "필드명-검증코드-기본메시지"
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RsData<Void>> handle(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .filter(error -> error instanceof FieldError)
                .map(error -> (FieldError) error)
                .map(error -> error.getField() + "-" + error.getCode() + "-" + error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("\n"));

        return ResponseEntity
                .status(BAD_REQUEST)
                .body(new RsData<>("400-1", message));
    }

    // JSON 형식 오류 → 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RsData<Void>> handle(HttpMessageNotReadableException ex) {
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(new RsData<>("400-1", "요청 본문이 올바르지 않습니다."));
    }
}
