package com.secondhand.micro.platform;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DuplicateKeyException;
@RestControllerAdvice
public class Errors {
 @ExceptionHandler(Failure.class) ResponseEntity<?> failure(Failure e){return ResponseEntity.status(e.status).body(Api.fail(e.code,e.getMessage()));}
 @ExceptionHandler({MethodArgumentNotValidException.class,MissingRequestHeaderException.class,HttpMessageNotReadableException.class})
 ResponseEntity<?> invalid(Exception e){return ResponseEntity.badRequest().body(Api.fail("BAD_REQUEST","参数缺失或格式错误"));}
 @ExceptionHandler(DuplicateKeyException.class) ResponseEntity<?> duplicate(Exception e){return ResponseEntity.status(409).body(Api.fail("CONFLICT","记录已存在"));}
}
