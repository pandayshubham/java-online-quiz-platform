# Java Online Quiz Platform

## Project Description

A Java Swing based online quiz platform for Java programming.

## Technology Stack

- Java 21
- Java Swing
- JDBC
- MySQL
- MySQL Connector/J
- Maven

---

## Database Setup & Configuration

### 1. Install & Start MySQL
- Ensure MySQL Server (version 8.0 or newer) is installed and the service is running.
- The platform uses a dedicated database named `quiz_platform`.

### 2. Create the Database Schema
Execute the `database.sql` script to create the `quiz_platform` database and all required tables:
```bash
mysql -u root -p < database.sql
```

### 3. Configure Database Credentials
Edit `src/main/resources/application.properties` with your local MySQL credentials:
```properties
db.url=jdbc:mysql://localhost:3306/quiz_platform?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=root
db.password=YOUR_PASSWORD
```
> *Note: Credentials are read dynamically at runtime and are never hardcoded in Java source files.*

### 4. Seed Initial Data
Execute `database_seed.sql` to populate default system settings and starter users:
```bash
mysql -u root -p < database_seed.sql
```

#### Seed Test Accounts (Academic / Demonstration):
- **Administrator**: `admin@quizplatform.com` / `admin123` (Role: `ADMIN`)
- **Quiz Creator 1**: `creator1@quizplatform.com` / `creator123` (Role: `QUIZ_CREATOR`)
- **Quiz Creator 2**: `creator2@quizplatform.com` / `creator123` (Role: `QUIZ_CREATOR`)
- **Participant 1**: `student1@quizplatform.com` / `student123` (Role: `PARTICIPANT`)
- **Participant 2**: `student2@quizplatform.com` / `student123` (Role: `PARTICIPANT`)
- **Participant 3**: `student3@quizplatform.com` / `student123` (Role: `PARTICIPANT`)

### 5. Verify Database Connection
Compile the project and run the standalone verification utility:
```bash
mvn clean test-compile
java -cp "target/classes;target/test-classes;<path-to-mysql-connector-j.jar>" com.quizplatform.DatabaseVerificationTest
```
Expected output:
- Driver loaded confirmation
- Connection established (`MySQL 8.0.x`)
- Active catalog matches `quiz_platform`
- Verification query (`SELECT 1`) succeeds
