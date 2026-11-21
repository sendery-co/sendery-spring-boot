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

Create a [project API key](https://sendery.co/en/docs/authentication) and store it as `SENDERY_API_KEY`. Add this property to `application.properties`.

```properties
# application.properties
sendery.api-key=${SENDERY_API_KEY}
```

## Send an email

Call this service with the recipient, published template key, variables, and a unique key for the email. [Keep them unchanged on retries](https://sendery.co/en/docs/idempotency). The returned `SendReceipt` contains `id()` and `status()`.

```java
import co.sendery.Sendery;
import co.sendery.SendReceipt;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class TemplateEmails {
    private final Sendery sendery;

    public TemplateEmails(Sendery sendery) {
        this.sendery = sendery;
    }

    public SendReceipt send(String to, String template, Map<String, Object> data, String emailKey) {
        return sendery.prepare(to, template, data, null, emailKey).retry(3).send();
    }
}
```

## Send a specific version

Choose a [published template version](https://sendery.co/en/docs/send-email#section-5) to keep sending it after newer versions are published. By default, Sendery uses the latest version.

```java
import co.sendery.Sendery;
import co.sendery.SendReceipt;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class TemplateEmails {
    private final Sendery sendery;

    public TemplateEmails(Sendery sendery) {
        this.sendery = sendery;
    }

    public SendReceipt send(String to, String template, Map<String, Object> data, String emailKey) {
        return sendery.prepare(to, template, data, null, emailKey).version(3).send();
    }
}
```

## Attachments

Pass a list of `Attachment` objects to your injected `Sendery` client. The SDK handles base64 encoding.

Send up to 10 files totaling 5 MB. See the [attachment reference](https://sendery.co/en/docs/send-email#section-6) for supported formats and limits.

```java
import co.sendery.Attachment;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

var file = Files.readAllBytes(Path.of("document.pdf"));

sendery.prepare(
    "alex@example.com",
    "your-template",
    Map.of("name", "Alex", "action_url", "https://example.com/start"),
    null,
    "your-idempotency-key",
    List.of(new Attachment("document.pdf", file, "application/pdf"))
).retry().send();
```

## Retrieve status and handle errors

Use [`sendery.get(id)`](https://sendery.co/en/docs/java) to retrieve status. API failures throw `SenderyException`; inspect `status()`, `code()`, `errors()`, and `retryAfter()`. Calls are blocking. The starter provides the Sendery template client; it does not configure `JavaMailSender`.

## More

Learn how to [retry emails without duplicate sends](https://sendery.co/en/docs/idempotency).

## License

[MIT](LICENSE).

## Events and workflows

Send events such as account signups or orders from your app. Your team can use them to automate emails in Sendery, choosing the content, recipients, and timing. Available on all plans.

Start with [Connecting your application](https://sendery.co/en/docs/connect-events) to define the data Sendery can expect, then [send events](https://sendery.co/en/docs/send-events) as they happen.

```java
var receipt = sendery.event("account.created", "account", "842",
    Map.of("email", "alex@example.com", "name", "Alex")
).retry().send();
```

Retries reuse the same event data and idempotency key. To retry across jobs or process restarts, supply and persist your own key. A successful response confirms that Sendery accepted the event. [Learn what you can automate with workflows](https://sendery.co/en/docs/build-workflow).
