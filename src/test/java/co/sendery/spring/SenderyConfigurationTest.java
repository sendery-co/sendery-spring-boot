package co.sendery.spring;

import co.sendery.Sendery;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.assertj.core.api.Assertions.assertThat;

class SenderyConfigurationTest {
    private final ApplicationContextRunner context = new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(SenderyAutoConfiguration.class));
    @Test void requiresAnApiKey() { context.run(app -> assertThat(app).doesNotHaveBean(Sendery.class)); }
    @Test void configuresClient() { context.withPropertyValues("sendery.api-key=test").run(app -> {
        assertThat(app).hasSingleBean(Sendery.class);
        var prepared = app.getBean(Sendery.class).prepare("a@example.com", "invoice", java.util.Map.of(), null, "invoice-1", java.util.List.of(new co.sendery.Attachment("invoice.pdf", new byte[] {1}, "application/pdf")));
        prepared.version(3);
        assertThat(prepared.idempotencyKey()).isEqualTo("invoice-1");
    }); }
    @Test void configuredStagingCredentialsAreUsedForRenderingAndSending() throws Exception {
        var requests = new java.util.concurrent.CopyOnWriteArrayList<String>();
        var server = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/v1/emails", exchange -> {
            requests.add(exchange.getRequestURI().getPath() + " " + exchange.getRequestHeaders().getFirst("Authorization"));
            exchange.getRequestBody().readAllBytes();
            boolean render = exchange.getRequestURI().getPath().endsWith("/render");
            var body = (render ? "{\"subject\":\"Hello\",\"html\":\"<p>Hello</p>\",\"text\":\"Hello\"}" : "{\"id\":\"mail\",\"status\":\"queued\",\"sandbox_url\":\"https://sendery.test/activity/mail\"}")
                .getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(render ? 200 : 202, body.length);
            exchange.getResponseBody().write(body); exchange.close();
        });
        server.start();
        try {
            context.withPropertyValues("sendery.api-key=snd_sandbox_staging", "sendery.url=http://127.0.0.1:" + server.getAddress().getPort()).run(app -> {
                var client = app.getBean(Sendery.class);
                assertThat(client.render("welcome", java.util.Map.of()).subject()).isEqualTo("Hello");
                assertThat(client.prepare("a@example.com", "welcome", java.util.Map.of()).send().sandboxUrl()).isEqualTo("https://sendery.test/activity/mail");
            });
            assertThat(requests).containsExactly("/api/v1/emails/render Bearer snd_sandbox_staging", "/api/v1/emails Bearer snd_sandbox_staging");
        } finally { server.stop(0); }
    }

    @Test void sendsEventsWithLanguageUsingTheConfiguredClient() throws Exception {
        var requests = new java.util.concurrent.CopyOnWriteArrayList<String>();
        var server = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/v1/events", exchange -> {
            requests.add(new String(exchange.getRequestBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
            var body = "{\"id\":\"event\",\"status\":\"accepted\",\"matched_workflows\":1}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(202, body.length);
            exchange.getResponseBody().write(body); exchange.close();
        });
        server.start();
        try {
            context.withPropertyValues("sendery.api-key=test", "sendery.url=http://127.0.0.1:" + server.getAddress().getPort()).run(app -> {
                var receipt = app.getBean(Sendery.class).event("account.created", "account", "42", java.util.Map.of("email", "user@example.com")).language("ja").send();
                assertThat(receipt.id()).isEqualTo("event");
            });
            var payload = com.google.gson.JsonParser.parseString(requests.get(0)).getAsJsonObject();
            assertThat(payload.get("language").getAsString()).isEqualTo("ja");
            assertThat(payload.getAsJsonObject("data").get("email").getAsString()).isEqualTo("user@example.com");
        } finally { server.stop(0); }
    }

    @Test void preservesCustomClient() {
        var client = new Sendery("custom");
        context.withPropertyValues("sendery.api-key=test").withBean(Sendery.class, () -> client).run(app -> assertThat(app.getBean(Sendery.class)).isSameAs(client));
    }
}
