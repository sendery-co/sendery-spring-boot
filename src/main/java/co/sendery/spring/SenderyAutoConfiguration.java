package co.sendery.spring;

import co.sendery.Sendery;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(SenderyProperties.class)
public class SenderyAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(Sendery.class)
    @ConditionalOnProperty(prefix = "sendery", name = "api-key")
    public Sendery sendery(SenderyProperties properties) { return new Sendery(properties.getApiKey(), properties.getUrl()); }
}
