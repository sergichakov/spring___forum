Предварительно нужно нужно собрать и установить библиотеку kafka-request-reply-util## Сборка
- Собирите коммандой mvn package -P prod
- Предварительно нужно нужно собрать и установить библиотеку kafka-request-reply-util коммандой mvn install -pl kafka-request-reply-util -am -P prod
## Приложение Forum 
Приложение построено согласно микросервисной архитектуре. Состоит следующих микросервисов:
- [topic-web - порт 18071](http://localhost:18071/topicsweb/swagger-ui/index.html)
- [directory-web - порт 18081](http://localhost:18081/directoriesweb/swagger-ui/index.html)
- [post-web - порт 18091](http://localhost:18091/postsweb/swagger-ui/index.html)
- [authorization-server - порт 9000 - http://localhost:9000/oauth2/authorize?response_type=code&client_id=gateway](http://localhost:9000/oauth2/authorize?response_type=code&client_id=gateway)
- [auth-gateway-server - порт 8080](http://localhost:8080/swagger-ui/index.html)
- [resource-server - порт 8090 со swagger-ui](http://localhost:8090/swagger-ui/index.html)
- [eureka-server - порт 8761](http://localhost:8761)
- [kafka-ui - порт 18080](http://localhost:18080)
- [kafka-exporter - порт 9308](http://localhost:9308)
- [prometheus - порт 9090](http://localhost:9090)
- [graphana - порт 3000](http://localhost:3000)
## Авторизация с использованием JWT. 
- В JWT добалено поле с именем userId обозначающее номер пользователя. Для получения access-token нужно:
- 1 - перейти на вкладку отладки с помощью F12, 
- 2 - перейти по ссылке [http://localhost:9000/oauth2/authorize?response_type=code&client_id=gateway](http://localhost:9000/oauth2/authorize?response_type=code&client_id=gateway)
- 3 - скопировать значение "code=" в окне F12 Отладки, и вставить значение поля code в значение code= в комманде 
curl --location --request POST 'http://localhost:9000/oauth2/token?grant_type=authorization_code&code=' \
--header 'Authorization: Basic Z2F0ZXdheTpzZWNyZXQ='
- 4 - если запустить комманду из п.3 то в JSON можно получить access_token.
## В форуме могут регистрироваться только два пользователя:
- admin:password
- user:password
## Рекомендую запускать микросервисы в следующий последовательности:
- docker compose up kafka-0 kafka-1 kafka-2 kafka-ui kafka-exporter prometheus grafana eureka-server resource-service postgres-users_repo
- docker compose up authorization-server auth-gateway-server postgres-topic postgres-directory postgres-post
- docker compose up topic-server topic-web directory-server directory-web post-server post-web

## В качестве брокера сообщений выбран Kafka 
По причине требования устойчивости при высокой нагрузке на инфраструктуру форума,
толерантности к сбоям. При использовании кафки и callbacks достигнуто полностью асинхронная передача данных 
из producer -> kafka -> consumer(ожидание базы) -> kafka -> producer. Это обеспечивает высокую пропускную способность микросервиса.
Описанные выше микросервисы регистрируются в eureka-server - реестр служб, позволяющий микрослужбам регистрировать себя 
и обнаруживать другие службы. Сервис Kafka состоит из трех серверов. При сбое одного из них будет разначен новый лидер топика 
и сообщения будут перенаправлены через partition выбранного лидера.
## В качестве базы данных
Для микросервисов выбрана БД Postgresql по причине надежности, использует богатый язык SQL, соотвествие требованиям ACID. 
## Мониторинг метрик Kafka 
Осуществляется посредством служб kafka-ui, Prometheus и Grafana.
