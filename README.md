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
  <version>0.1.1</version>
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

## Attachments

Pass a list of `Attachment` objects to your injected `Sendery` client. The SDK handles base64 encoding.

Send up to 10 files totaling 5 MB. See the [attachment reference](https://sendery.co/en/docs/send-email#section-5) for supported formats and limits.

```java
import co.sendery.Attachment;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

var file = Files.readAllBytes(Path.of("document.pdf"));

sendery.prepare(
    "alex@example.com",
    "welcome",
    Map.of("name", "Alex", "action_url", "https://example.com/start"),
    null,
    "welcome-attachment-123",
    List.of(new Attachment("document.pdf", file, "application/pdf"))
).retry().send();
```

## Retrieve status and handle errors

Use [`sendery.get(id)`](https://sendery.co/en/docs/java) to retrieve status. API failures throw `SenderyException`; inspect `status()`, `code()`, `errors()`, and `retryAfter()`. Calls are blocking. The starter provides the Sendery template client; it does not configure `JavaMailSender`.

## More

See [idempotency and retries](https://sendery.co/en/docs/idempotency) for retry conditions, delays, and reusing a key across attempts.

## License

[MIT](LICENSE).
