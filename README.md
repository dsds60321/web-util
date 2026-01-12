# TableGen (HTML Generator)

## 📖 개요
**TableGen**은 데이터베이스(MariaDB/MySQL)의 테이블 정보를 읽어와 **HTML 테이블 코드**를 자동으로 생성해주는 도구입니다.
단순 HTML 뿐만 아니라 **Thymeleaf**, **Mustache**와 같은 템플릿 엔진 문법에 맞춘 코드 생성도 지원합니다.

CLI(Command Line Interface) 환경과 웹(Spring Boot) 환경을 모두 지원하여 상황에 맞게 사용할 수 있습니다.

## ✨ 주요 기능
- **DB 메타데이터 연동**: 데이터베이스에 접속하여 테이블의 컬럼 정보(이름, 타입, 주석 등)를 자동으로 조회합니다.
- **다양한 템플릿 지원**:
  - `HTML`: 순수 HTML 테이블 (`<table>`)
  - `THYMELEAF`: Spring Boot Thymeleaf 문법 적용 (`th:each`, `th:text`)
  - `MUSTACHE`: Mustache 문법 적용 (`{{#list}}`, `{{value}}`)
- **필터링 기능**: 특정 컬럼만 포함(`--only`)하거나 제외(`--exclude`)할 수 있습니다.
- **클립보드 복사 (CLI)**: 생성된 코드를 파일로 저장하거나 즉시 클립보드로 복사할 수 있습니다.
- **인터랙티브 모드 (CLI)**: 인자 없이 실행 시 대화형으로 설정을 입력받습니다.
- **설정 파일 지원**: `tablegen.properties` 파일을 통해 자주 사용하는 DB 접속 정보를 관리할 수 있습니다.

## 🛠 시스템 요구사항
- **Java**: 17 이상
- **Maven**: 3.x 이상
- **Database**: MariaDB 또는 MySQL

## 🚀 빌드 및 설치

이 프로젝트는 **Maven Profile**을 사용하여 **웹 서버용**과 **CLI 도구용** JAR를 분리하여 빌드할 수 있습니다.

### 1. 웹 서버용 빌드 (기본값)
스프링 부트 웹 서버를 실행하기 위한 JAR 파일을 생성합니다.

```bash
mvn clean package
```
- 생성 파일: `target/tablegen-web.jar`

### 2. CLI 도구용 빌드
터미널에서 명령어로 실행하기 위한 독립적인 JAR 파일을 생성합니다.

```bash
mvn clean package -Pcli
```
- 생성 파일: `target/tablegen-cli.jar`

---

## 💻 사용 방법 (CLI)

CLI 모드는 `tablegen-cli.jar` 파일을 사용합니다.

### 1. 인터랙티브 모드 (권장)
인자 없이 실행하면 단계별로 설정을 입력받는 대화형 모드가 시작됩니다.

```bash
java -jar target/tablegen-cli.jar
```

### 2. 명령어 인자 모드
모든 옵션을 명령줄 인자로 전달하여 즉시 실행합니다.

```bash
java -jar target/tablegen-cli.jar \
    --db jdbc:mariadb://localhost:3306/mydb \
    --user root \
    --pass password \
    --schema mydb \
    --table user_tb \
    --template THYMELEAF \
    --copy
```

### 3. CLI 옵션 목록

| 옵션 | 설명 | 예시 |
|------|------|------|
| `--db` | DB JDBC URL | `jdbc:mariadb://localhost:3306/db` |
| `--user` | DB 사용자명 | `root` |
| `--pass` | DB 비밀번호 | `1234` |
| `--schema` | 데이터베이스 스키마명 | `public` |
| `--table` | 대상 테이블명 | `users` |
| `--template` | 템플릿 타입 (`HTML`, `THYMELEAF`, `MUSTACHE`) | `THYMELEAF` |
| `--out` | 출력 파일 경로 (생략 시 콘솔 출력) | `result.html` |
| `--copy` | 결과 클립보드 복사 여부 (Flag) | `--copy` |
| `--only` | 포함할 컬럼 지정 (쉼표 구분) | `id,name,email` |
| `--exclude` | 제외할 컬럼 지정 (쉼표 구분) | `created_at,updated_at` |

---

## 🌐 사용 방법 (Web)

웹 모드는 `tablegen-web.jar` 파일을 사용합니다.

### 실행

```bash
java -jar target/tablegen-web.jar
```

### 접속
브라우저를 열고 아래 주소로 접속합니다.
- URL: `http://localhost:8080`

---

## ⚙️ 설정 파일 (tablegen.properties)

매번 DB 정보를 입력하기 번거롭다면, 실행 위치 또는 사용자 홈 디렉토리(`~/.tablegen/`)에 `tablegen.properties` 파일을 생성하여 기본값을 설정할 수 있습니다.

**tablegen.properties 예시:**
```properties
db.url=jdbc:mariadb://localhost:3306/my_schema
db.user=devuser
db.pass=devpass!
db.schema=my_schema
template=THYMELEAF
```# web-util
