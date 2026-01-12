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

프로젝트 루트 디렉토리에서 아래 명령어를 실행하여 빌드합니다.

```bash
mvn clean package
```

빌드가 완료되면 `target/tablegen-1.0-SNAPSHOT.jar` 파일이 생성됩니다.

---

## 💻 사용 방법 (CLI)

CLI 도구는 `com.tablegen.Main` 클래스를 통해 실행됩니다.

### 1. 인터랙티브 모드 (권장)
별도의 인자 없이 실행하면 대화형 모드가 시작됩니다.

```bash
java -cp target/tablegen-1.0-SNAPSHOT.jar -Dloader.main=com.tablegen.Main org.springframework.boot.loader.launch.PropertiesLauncher
```
*(참고: Spring Boot Fat Jar 구조로 패키징되므로 `PropertiesLauncher`를 사용하여 Main 클래스를 호출해야 합니다. 개발 환경에서는 IDE나 `mvn exec:java`를 사용하는 것이 편리합니다.)*

### 2. 명령어 인자 모드
필요한 옵션을 직접 지정하여 실행할 수 있습니다.

```bash
java -cp target/tablegen-1.0-SNAPSHOT.jar -Dloader.main=com.tablegen.Main org.springframework.boot.loader.launch.PropertiesLauncher \
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

웹 애플리케이션 모드는 브라우저에서 UI를 통해 코드를 생성할 수 있습니다.

### 실행

```bash
java -jar target/tablegen-1.0-SNAPSHOT.jar
```

### 접속
브라우저를 열고 `http://localhost:8080` 으로 접속합니다. (포트는 설정에 따라 다를 수 있음)

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
