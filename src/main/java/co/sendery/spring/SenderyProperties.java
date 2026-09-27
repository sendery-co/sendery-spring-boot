package co.sendery.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("sendery")
public class SenderyProperties {
    private String apiKey;
    private String url = "https://sendery.co";
    public String getApiKey() { return apiKey; }
    public void setApiKey(String key) { apiKey = key; }
    public String getUrl() { return url; }
    public void setUrl(String value) { url = value; }
}
