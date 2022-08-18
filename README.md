### Ordering API Implementation

##### Summary
Simple Ordering Backend API application with JWT Security

##### API's

- Authentication API
- Order API

```
POST /api/register - Registers new user
POST /api/auth - Logs a new user in and returns Access Token

POST /api/orders - Creates single or multiple orders
GET /api/orders - List all orders
GET /api/orders/user?userId - List all orders of User
PATCH /api/orders/:orderId - Cancel an order
DELET /api/orders/:orderId - Deletes an order
```

##### Tech Stack
- Java 11
- Spring Boot
- Spring Data JPA
- Spring Security
- Restful API
- MySQL:5.7
- Docker
- Docker compose
- Swagger
- Lombok
- JJWT
- Spring Boot Validation API

##### Prerequisites
- Maven
- Docker

##### To Run Tests
```
mvn test
```

##### Run & Build
```
$ cd order-api
$ docker-compose up -d
```

```
Note: Rename .env.dev file to .env and replace existing fields 
with your informations before executing docker-compose
```