# OrderMS

OrderMS é um serviço Spring Boot para registrar pedidos recebidos pelo RabbitMQ e consultar os pedidos de um cliente. Os pedidos são armazenados no MongoDB.

## Tecnologias

- Java 21
- Spring Boot 4.1.1
- Maven (incluído pelo Maven Wrapper)
- MongoDB
- RabbitMQ
- JUnit 5 e Mockito para testes

## Pré-requisitos

- JDK 21 instalado e `JAVA_HOME` configurado.
- Docker com o plugin Docker Compose.

O Maven não precisa ser instalado: use o wrapper incluído no projeto.

## Executar localmente

Na raiz do projeto, inicie o MongoDB e o RabbitMQ:

```powershell
docker compose -f local/docker-compose.yml up -d
```

Em seguida, inicie a aplicação:

```powershell
.\mvnw.cmd spring-boot:run
```

A aplicação fica disponível em `http://localhost:8080`. A configuração local da aplicação está em `src/main/resources/application.properties`; o Compose disponibiliza o MongoDB na porta `27017` e o RabbitMQ nas portas `5672` (mensagens) e `15672` (console de gerenciamento).

Para parar os serviços locais:

```powershell
docker compose -f local/docker-compose.yml down
```

## Recebimento de pedidos

A aplicação consome eventos da fila `btg-pactual-order-created`. Cada evento contém o código do pedido, o código do cliente e seus itens, por exemplo:

```json
{
  "orderCode": 1,
  "clientCode": 42,
  "items": [
    {
      "product": "Produto",
      "quantity": 2,
      "price": 19.90
    }
  ]
}
```

O valor total do pedido é calculado a partir da quantidade e do preço de cada item antes de salvá-lo.

## API

`GET /customers/{customerId}/orders` lista os pedidos do cliente e o total acumulado. Os parâmetros opcionais `page` (padrão `0`) e `pageSize` (padrão `10`) controlam a paginação.

Exemplo:

```text
GET http://localhost:8080/customers/42/orders?page=0&pageSize=10
```

A resposta contém o resumo com `totalOnOrders`, os pedidos da página em `data` e os metadados de paginação em `pagination`.

## Testes

Execute os testes unitários e de contexto com:

```powershell
.\mvnw.cmd test
```

Os testes unitários do serviço usam JUnit 5 e Mockito e não precisam de conexões reais com MongoDB ou RabbitMQ.
