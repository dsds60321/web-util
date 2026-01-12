# TableGen (HTML Generator)

데이터베이스의 테이블 메타데이터를 기반으로 HTML 테이블 코드를 자동 생성해주는 유틸리티입니다.

## 📖 개요
**TableGen**은 데이터베이스(MariaDB, MySQL, PostgreSQL, Oracle)의 테이블 정보를 읽어와 **HTML 테이블 코드**를 자동으로 생성해주는 도구입니다. 단순 HTML 뿐만 아니라 **Thymeleaf**, **Mustache**와 같은 인기 있는 템플릿 엔진 문법에 맞춘 코드 생성도 지원하여 개발 생산성을 높여줍니다.

CLI(Command Line Interface) 환경과 웹(Spring Boot) 환경을 모두 제공하여 사용자의 환경에 맞게 선택하여 사용할 수 있습니다.

## ✨ 주요 기능
- **다양한 DB 지원**: MariaDB, MySQL, PostgreSQL, Oracle 등 주요 RDBMS 지원
- **DB 메타데이터 연동**: 테이블의 컬럼 이름, 타입, 코멘트(주석) 정보를 자동으로 추출
- **다양한 템플릿 지원**:
  - `HTML`: 순수 HTML5 테이블 구조 (`<table>`, `<thead>`, `<tbody>` 등)
  - `THYMELEAF`: Spring Boot에서 사용되는 Thymeleaf 문법 (`th:each`, `th:text`) 적용
  - `MUSTACHE`: Mustache 템플릿 엔진 문법 (`{{#list}}`, `{{value}}`) 적용
- **필터링 옵션**: 특정 컬럼만 포함(`--only`)하거나 불필요한 컬럼을 제외(`--exclude`) 가능
- **편의 기능 (CLI)**:
  - 생성된 코드를 즉시 클립보드에 복사 (`--copy`)
  - 대화형(Interactive) 모드를 통한 손쉬운 설정 입력
  - 결과물을 파일로 직접 저장 (`--out`)
- **설정 파일 지원**: `tablegen.properties`를 통해 반복되는 DB 접속 정보를 관리

## 🛠 시스템 요구사항
- **Java**: 17 이상
- **Maven**: 3.x 이상
- **Database**: MariaDB, MySQL, PostgreSQL, Oracle (JDBC 드라이버 내장)

## 🚀 빌드 및 설치

이 프로젝트는 Maven Profile을 사용하여 웹 서버용과 CLI 도구용 JAR를 분리하여 빌드할 수 있습니다.

### 1. 웹 서버용 빌드 (Default)
브라우저에서 UI를 통해 사용하고 싶을 때 빌드합니다.
```bash
mvn clean package
```
- 생성 파일: `target/tablegen-web.jar`

### 2. CLI 도구용 빌드
터미널에서 명령어로 빠르게 실행하고 싶을 때 빌드합니다.
```bash
mvn clean package -Pcli
```
- 생성 파일: `target/tablegen-cli.jar`

---

## 💻 사용 방법 (CLI 모드)

CLI 모드는 `tablegen-cli.jar`를 사용하여 터미널에서 실행합니다.

### 1. 인터랙티브 모드 (추천)
인자 없이 실행하면 필요한 정보를 단계별로 묻는 대화형 모드가 시작됩니다.
```bash
java -jar target/tablegen-cli.jar
```

### 2. 명령어 인자 모드
모든 옵션을 명령줄 인자로 전달하여 즉시 결과를 얻습니다.
```bash
java -jar target/tablegen-cli.jar \
    --db jdbc:mariadb://localhost:3306/mydb \
    --user root \
    --pass password \
    --schema mydb \
    --table users \
    --template THYMELEAF \
    --copy
```

### 3. CLI 옵션 목록

| 옵션 | 설명 | 예시 |
|------|------|------|
| `--db` | DB JDBC URL | `jdbc:postgresql://localhost:5432/db` |
| `--user` | DB 사용자명 | `scott` |
| `--pass` | DB 비밀번호 | `tiger` |
| `--schema` | 데이터베이스 스키마명 | `public` |
| `--table` | 대상 테이블명 | `orders` |
| `--template` | 템플릿 타입 (`HTML`, `THYMELEAF`, `MUSTACHE`) | `MUSTACHE` |
| `--out` | 출력 파일 경로 (생략 시 테이블명.html 생성) | `output.html` |
| `--copy` | 결과 클립보드 복사 여부 (Flag) | `--copy` |
| `--only` | 포함할 컬럼 지정 (쉼표 구분) | `id,title,content` |
| `--exclude` | 제외할 컬럼 지정 (쉼표 구분) | `created_at,updated_at` |

---

## 🌐 사용 방법 (Web 모드)

웹 모드는 Spring Boot 서버를 실행하여 브라우저에서 사용합니다.

### 1. 서버 실행
```bash
java -jar target/tablegen-web.jar
```

### 2. 접속 및 사용
브라우저를 열고 다음 주소로 접속합니다.
- **URL**: `http://localhost:8080`
- 화면의 폼에 DB 정보 및 테이블명을 입력하고 'Generate' 버튼을 클릭합니다.

---

## ⚙️ 설정 파일 (tablegen.properties)

매번 DB 접속 정보를 입력하기 번거로운 경우, 프로젝트 루트 디렉토리 또는 사용자의 홈 디렉토리(`~/.tablegen/`)에 `tablegen.properties` 파일을 작성해 두면 자동으로 기본값으로 로드됩니다.

**tablegen.properties 예시:**
```properties
db.url=jdbc:mariadb://localhost:3306/my_schema
db.user=devuser
db.pass=devpass!
db.schema=my_schema
template=THYMELEAF
```

```