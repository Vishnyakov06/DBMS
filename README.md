#  **Проект "СУБД"**  
**Spring Boot приложение для управления записями с CRUD-операциями и базой данных PostgreSQL**  

---

##  **Основные возможности**  
✅ **Полный CRUD-функционал** (создание, чтение, обновление, удаление записей)  
✅ **Работа с PostgreSQL** (хранение и управление данными)  
✅ **REST API** (доступ через HTTP-запросы)  
✅ **Валидация данных** (проверка входных параметров)  
✅ **Логирование операций** (отслеживание действий в системе)  

---

##  **Стек технологий**  
- **Backend**:  
  - Java 17  
  - Spring Boot 3  
  - Spring Web (REST API)  
  - Spring Data JPA (работа с БД)  
  - PostgreSQL (реляционная СУБД)  
  - Lombok (упрощение кода)  
- **Дополнительно**:  
  - Swagger/OpenAPI (документация API)  
  - Log4j/SLF4J (логирование)  

---

##  **Запуск проекта**  

### **1. Требования**  
- Установленные:  
  - Java 17+  
  - Maven
  - PostgreSQL 17+  

### **2. Настройка базы данных**  
1. Создайте БД в PostgreSQL:  
   ```sql
   CREATE DATABASE task_manager;
   ```
2. Настройте подключение в `application.yml`:  
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/task_manager
       username: ваш_логин
       password: ваш_пароль
     jpa:
       hibernate:
         ddl-auto: update
   ```

### **3. Сборка и запуск**  
```bash
mvn spring-boot:run 
```  
Приложение будет доступно по адресу: [http://localhost:8080](http://localhost:8080)  

---

##  **Что отработано в проекте**  
- Настройка Spring Data JPA + PostgreSQL.  
- Реализация CRUD-операций через REST.  
- Валидация входных данных (`@Valid`, `@NotNull`).  
- Обработка ошибок (HTTP-статусы 400, 404, 500).  
- Логирование ключевых действий.  

---
