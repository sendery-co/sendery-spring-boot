# Sendery for Spring Boot

Inject a Sendery client into your Spring Boot services.

[Documentation](https://sendery.co/en/docs/spring-boot) · [API reference](https://sendery.co/en/docs/send-email) · [Changelog](CHANGELOG.md)

## Requirements

Spring Boot 4.1 and Java 17+.

## Install

```xml
<dependency>
  <groupId>co.sendery</groupId>
  <artifactId>sendery-spring-boot-starter</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Configure your application

Publish a `welcome` template with `name` and `action_url` variables, and create a [project API key](https://sendery.co/en/docs/authentication). Store it as `SENDERY_API_KEY` on your server. Add this property to `application.properties`.

```properties
# application.properties
sendery.api-key=${SENDERY_API_KEY}
```

## Send an email

Inject `Sendery` and call this service with the recipient, name, and a unique event key. [Keep those values unchanged on retries](https://sendery.co/en/docs/idempotency). The returned `SendReceipt` contains `id()` and `status()`.

```java
import co.sendery.Sendery;
import co.sendery.SendReceipt;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class WelcomeEmails {
    private final Sendery sendery;

    public WelcomeEmails(Sendery sendery) {
        this.sendery = sendery;
    }

    public SendReceipt send(String address, String name, String eventKey) {
        return sendery.prepare(address, "welcome", Map.of(
            "name", name,
            "action_url", "https://example.com/start"
        ), null, eventKey).retry(3).send();
    }
}
```

## Retrieve status and handle errors

Use [`sendery.get(id)`](https://sendery.co/en/docs/java) to retrieve status. API failures throw `SenderyException`; inspect `status()`, `code()`, `errors()`, and `retryAfter()`. Calls are blocking. The starter provides the Sendery template client; it does not configure `JavaMailSender`.

## More

See [idempotency and retries](https://sendery.co/en/docs/idempotency) for retry conditions, delays, and reusing a key across attempts.

## License

[MIT](LICENSE).
