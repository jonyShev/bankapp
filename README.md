# Bank System (Microservices)

Микросервисная банковская система.
Стек: Java 17, Spring Boot 3, Spring Cloud, PostgreSQL, Keycloak, Consul, Docker Compose.

## Архитектура

Все сервисы регистрируются в Consul. Входная точка — Spring Cloud Gateway (8080).
Сервисы:
gateway — маршрутизация (порт 8080).
front-ui — HTML-шаблоны /main, /signup, форма логина (порт 8081).
accounts — пользователи и счета, схема БД accounts, защищённые /api/internal/**.
exchange — курсы валют, схема БД exchange.
exchange-generator — генерация курсов и запись в exchange по JWT.
cash — пополнение/снятие, S2S токен.
transfer — переводы «себе» и «другому», конвертация через RUB, S2S токен.
blocker — антифрод (лимиты/случайные блокировки).
notifications — приём и логирование событий операций.
Браузер обращается только к gateway. Внешние маршруты не проксируют /api/internal/**.

## Сборка

## !!!Важно!!! 
в случае возниконовения ошибок необходимо удалить папки target руками!


```bash
./mvnw -DskipTests package
```

## Запуск через Docker Compose

```bash
docker-compose up --build
```

## Тесты

```bash
./mvnw test
```

## Версия
v1.0 - микросервисы, сервис-дискавери, gateway, распределённая конфигурация, персистентность, 
аутентификация пользователей, аутентификация межсервисных вызовов, обработка основных бизнес-операций, 
миграции, тесты и запуск через Docker Compose.








## 🧱 Основной функционал

✅ Регистрация с проверкой возраста ≥ 18 лет и авто-логином  
✅ Form-login через Spring Security  
✅ Просмотр профиля и всех валютных счетов (`exists =true/false`)  
✅ Пополнение / снятие средств (`cashErrors`)  
✅ Обмен валют между своими счетами (`transferErrors`)  
✅ Перевод другому пользователю (`transferOtherErrors`)  
✅ Блокировка подозрительных операций (anti-fraud service `blocker`)  
✅ Отправка уведомлений в `notifications` после успешных действий  
✅ Настройки аккаунта:
- смена пароля (`passwordErrors`)
- редактирование профиля (ФИО, дата рождения)
- управление валютными счетами (добавление/удаление нулевых)  
  ✅ Централизованный конфиг через **Consul KV**  
  ✅ Защищённые внутренние эндпоинты `/api/internal/**`  
  ✅ Автоматические миграции Flyway (схемы `accounts`, `exchange`)  
  ✅ Тесты: unit / integration (Testcontainers Postgres) / contract (Spring Cloud Contract)

---

## 🗄 Конфигурация через Consul KV

Consul UI — [http://localhost:8500](http://localhost:8500)

### Глобальные параметры
`config/application/data`