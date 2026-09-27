package co.sendery.spring;

import co.sendery.Sendery;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.assertj.core.api.Assertions.assertThat;

class SenderyConfigurationTest {
    private final ApplicationContextRunner context = new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(SenderyAutoConfiguration.class));
    @Test void requiresAnApiKey() { context.run(app -> assertThat(app).doesNotHaveBean(Sendery.class)); }
    @Test void configuresClient() { context.withPropertyValues("sendery.api-key=test").run(app -> assertThat(app).hasSingleBean(Sendery.class)); }
    @Test void preservesCustomClient() {
        var client = new Sendery("custom");
        context.withPropertyValues("sendery.api-key=test").withBean(Sendery.class, () -> client).run(app -> assertThat(app.getBean(Sendery.class)).isSameAs(client));
    }
}
