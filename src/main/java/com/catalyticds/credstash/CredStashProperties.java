package com.catalyticds.credstash;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author reesbyars on 9/22/17.
 */
@ConfigurationProperties(prefix = "credstash")
class CredStashProperties extends CredStashPropertyConfig {

    private Boolean enabled = false;
    private String pathSeparator = ".";
    private Map<String, CredStashPropertyConfig> more = new LinkedHashMap<>();

    public CredStashProperties() {
        setName("defaults");
        setTable("credential-store");
        setAddPrefix("");
        getMatching().add("");
        setVersion(null);
        setStripPrefix(null);
        setContext(new LinkedHashMap<>());
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getPathSeparator() {
        return pathSeparator;
    }

    public void setPathSeparator(String pathSeparator) {
        this.pathSeparator = pathSeparator;
    }

    public Map<String, CredStashPropertyConfig> getMore() {
        return more;
    }

    public void setMore(Map<String, CredStashPropertyConfig> more) {
        this.more = more;
    }

    List<CredStashPropertyConfig> compileToOrderedList() {
        List<CredStashPropertyConfig> configs = more
                .entrySet()
                .stream()
                .map(entry -> entry.getValue().withNameAndDefaults(entry.getKey(), this))
                .collect(Collectors.toList());
        configs.add(this);
        return configs;
    }

    @Override
    public String toString() {
        return "CredStashProperties{" +
                "enabled=" + enabled +
                ", pathSeparator='" + pathSeparator + '\'' +
                ", defaults=" + super.toString() +
                ", more=" + more +
                '}';
    }
}
