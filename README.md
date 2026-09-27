# Sendery — Spring Boot integration

Send template emails from Spring Boot with the Sendery SDK.

MIT licensed. Repository: https://github.com/sendery-co/sendery-spring-boot

Documentation: https://sendery.co/en/docs/spring-boot

## Install

```
<dependency>
  <groupId>co.sendery</groupId>
  <artifactId>sendery-spring-boot-starter</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Install

Spring Boot 4.1 / Java 17+

## Configure properties

Set sendery.api-key=${SENDERY_API_KEY} in application.properties. The starter creates a Sendery bean when the key is configured. sendery.url can override the base URL.

## Inject the client

Inject Sendery into your service and send a published template. Defining your own Sendery bean overrides auto-configuration.

## Mail integration

This starter supplies a template client, not an arbitrary JavaMailSender replacement. Keep your existing mailer for emails that have not been mapped to a Sendery template. Persist the event key and variables for scheduled or queued work.

## Configuration example

```
# application.properties
sendery.api-key=${SENDERY_API_KEY}
```

## Example

```
import co.sendery.Sendery;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class WelcomeEmails {
    private final Sendery sendery;
    public WelcomeEmails(Sendery sendery) { this.sendery = sendery; }
    public void send(String address, String name, String eventId) {
        sendery.prepare(address, "welcome", Map.of("name", name), null, eventId)
            .retry(3).send();
    }
}
```

## Retries and queues

Reuse a prepared email for retries. New requests receive new keys; when reconstructing a request in another process, supply the original key and unchanged data. Keep API keys server-side. Framework mailers send Sendery templates, not arbitrary HTML or attachments.
