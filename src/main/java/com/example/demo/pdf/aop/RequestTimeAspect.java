package com.example.demo.pdf.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Aspect
@Component
public class RequestTimeAspect {

    @Around("@annotation(PDFTimeCheck)")
    public Object measureTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        System.out.println("생성 시작 시간 : " + sysToLocalDateTime(startTime));
        try {
            Object result = joinPoint.proceed(); // 메서드 실행
            if (result instanceof ResponseEntity) {
                long endTime = System.currentTimeMillis();
                long durationInSeconds = (endTime - startTime); // 밀리초를 초로 변환
                System.out.println("생성 완료 시간 : " + sysToLocalDateTime(endTime));
                System.out.println("생성 소요 시간 : " + durationInSeconds + "ms");

                // 결과가 ResponseEntity<byte[]>라면, 새로운 ResponseEntity를 생성
                ResponseEntity<byte[]> response = (ResponseEntity<byte[]>) result;

                HttpHeaders headers = new HttpHeaders();
                headers.putAll(response.getHeaders());

                // X-PDF-Generation-Time 헤더 추가
                headers.add("X-PDF-Generation-Start-Time", sysToLocalDateTime(startTime));
                headers.add("X-PDF-Generation-End-Time", sysToLocalDateTime(endTime));
                headers.add("X-PDF-Generation-Time", durationInSeconds + "ms");

                // 새로운 ResponseEntity 반환
                return new ResponseEntity<>(response.getBody(), headers, response.getStatusCode());
            }
            return result;
        } catch (Throwable throwable) {
            throw throwable;
        }
    }

    private String sysToLocalDateTime(Long sysTime) {;

        // 밀리초 값을 Instant로 변환합니다.
        Instant instant = Instant.ofEpochMilli(sysTime);

        // Instant를 LocalDateTime으로 변환 (기본 타임존 사용)
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        // 초 단위까지 포맷을 설정하여 출력합니다.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        return localDateTime.format(formatter);

    }


}