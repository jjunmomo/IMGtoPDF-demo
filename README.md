# PDF 변환 및 병합 모듈

이 프로젝트는 이미지를 PDF 파일로 변환하고, 생성된 여러 PDF 파일을 병합하는 기능을 제공.
PDF 파일 생성과 병합에 관한 REST API를 제공.

## 기능 요약

1. **이미지에서 PDF 파일 생성**
    - 지정된 경로에 이미지를 기반으로 PDF 파일을 생성함.
    - 생성된 PDF 파일에는 기능 설명 및 수정 요청 사항이 포함함.

2. **생성된 PDF 리스트 조회**
    - 현재 서버에서 생성된 PDF 파일 목록을 조회.
    - 서버 종료 또는 병합 후에는 리스트가 초기화.

3. **PDF 파일 병합**
    - `first_page.pdf`와 생성된 PDF 파일들을 하나의 PDF로 병합.
    - 병합 후 원래의 PDF 파일들은 삭제.

## API 명세

### 1. 이미지에서 PDF 파일 생성

- **URL**: `/generate`
- **Method**: `POST`
- **Request Parameters**:
    - `image`: 변환할 이미지 파일 (multipart/form-data)
    - `content`: 기능 설명 (String)
    - `modificationRequirements`: 수정 요청 사항 (String)
- **Response**: 생성된 PDF 파일의 내용 (byte array)
- **설명**:
    - PDF 파일을 저장하고, 클라이언트에게 생성된 PDF 파일을 응답합니다.
    - PDF 파일은 `/pdf/` 경로에 저장됩니다. 경로가 존재하지 않으면 자동으로 생성됩니다.

### 2. 생성된 PDF 리스트 조회

- **URL**: `/generate`
- **Method**: `GET`
- **Response**: 생성된 PDF 파일들의 리스트 (string array)
- **설명**:
    - 현재 서버에서 생성된 PDF 파일의 이름과 생성 소요시간을 조회.
    - 서버 종료나 PDF 병합 이후에는 리스트가 초기화.

### 3. PDF 파일 병합

- **URL**: `/merge`
- **Method**: `POST`
- **Response**: 병합된 PDF 파일의 내용 (byte array)
- **설명**:
    - `first_page.pdf`와 생성된 PDF 리스트를 하나의 PDF로 병합.
    - 병합된 PDF 파일은 `/pdf/` 경로에 `merge_XX.pdf`라는 이름으로 저장.
    - 병합 후 생성된 개별 PDF 파일들은 삭제.

## 디렉토리 구조

- **PDF 저장 경로**: `/pdf/`
    - 서버 실행 시 사용자의 홈 디렉토리 아래 `pdf` 폴더가 존재하지 않으면 자동으로 생성.

## 요구 사항

- Java 11 이상
- Spring Boot 2.7.6
- Apache PDFBox


