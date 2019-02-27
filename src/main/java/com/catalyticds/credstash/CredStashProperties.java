package com.catalyticds.credstash;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author reesbyars on 9/22/17.
 */
@RefreshScope
@ConfigurationProperties(prefix = "credstash")
class CredStashProperties extends CredStashPropertyConfig {

    private String pathSeparator = ".";
    private Mode mode = Mode.PROD;
    private Map<String, CredStashPropertyConfig> properties = new LinkedHashMap<>();

    public CredStashProperties() {
        setName("root");
        setTable("credential-store");
        setEnabled(false);
        setAddPrefix("");
        setVersion(null);
        setStripPrefix(null);
        setContext(new LinkedHashMap<>());
    }

    public String getPathSeparator() {
        return pathSeparator;
    }

    public void setPathSeparator(String pathSeparator) {
        this.pathSeparator = pathSeparator;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Map<String, CredStashPropertyConfig> getProperties() {
        return properties;
    }

    List<CredStashPropertyConfig> compileToOrderedList() {
        CredStashPropertyConfig defaultConfig = new CredStashPropertyConfig();
        defaultConfig.getMatching().addAll(getMatching());
        defaultConfig.getMatching().add(new PropertyEntry("credstash__*.**"));
        defaultConfig.setStripPrefix("credstash__");
        defaultConfig.getSources().addAll(getSources());
        properties.put("default", defaultConfig);
        return properties.entrySet().stream()
                .map(p -> p.getValue().withDefaults(p.getKey(), this))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "CredStashProperties{" +
                "pathSeparator='" + pathSeparator + '\'' +
                ", defaults=" + super.toString() +
                '}';
    }

    public enum Mode {
        PROD, AUDIT
    }
}
