package com.example.demo.pdf.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RequestTimeAspect {

    @Around("@annotation(PDFTimeCheck)")
    public Object measureTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed(); // 메서드 실행
            if (result instanceof ResponseEntity) {
                long endTime = System.currentTimeMillis();
                long durationInSeconds = (endTime - startTime); // 밀리초를 초로 변환

                // 결과가 ResponseEntity<byte[]>라면, 새로운 ResponseEntity를 생성
                ResponseEntity<byte[]> response = (ResponseEntity<byte[]>) result;

                HttpHeaders headers = new HttpHeaders();
                headers.putAll(response.getHeaders());

                // X-PDF-Generation-Time 헤더 추가
                headers.add("X-PDF-Generation-Time", durationInSeconds + "ms");

                // 새로운 ResponseEntity 반환
                return new ResponseEntity<>(response.getBody(), headers, response.getStatusCode());
            }
            return result;
        } catch (Throwable throwable) {
            throw throwable;
        }
    }


}