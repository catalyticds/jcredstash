package com.catalyticds.credstash;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.AntPathMatcher;

import java.util.List;


/**
 * @author reesbyars on 9/22/17.
 */
@Configuration
@EnableConfigurationProperties(CredStashProperties.class)
@ConditionalOnProperty(prefix = "credstash", name = "enabled", havingValue = "true")
public class CredStashBootstrapConfiguration implements PropertySourceLocator {

    private final CredStashProperties credStashProperties;
    private final List<CredStashPropertyConfig> credStashPropertyConfigs;

    CredStashBootstrapConfiguration(
            CredStashProperties credStashProperties) {
        this.credStashProperties = credStashProperties;
        this.credStashPropertyConfigs = credStashProperties.compileToOrderedList();
    }

    @Bean
    PropertySource<CredStash> credStashPropertySource() {
        return new CredStashPropertySource(
                credStash(),
                credStashPropertyConfigs,
                credStashProperties.getMode(),
                new AntPathMatcher(credStashProperties.getPathSeparator()));
    }

    @Bean
    @ConditionalOnMissingBean
    CredStashCrypto credStashCrypto() {
        return new CredStashBouncyCastleCrypto();
    }

    @Bean
    @ConditionalOnMissingBean
    public CredStash credStash() {
        return new CredStash(
                AmazonDynamoDBClientBuilder.defaultClient(),
                AWSKMSClientBuilder.defaultClient(),
                credStashCrypto());
    }

    @Override
    public PropertySource<?> locate(Environment environment) {
        return credStashPropertySource();
    }

    @Bean
    @ConditionalOnProperty(prefix = "credstash", name = "encryptor.secret.name")
    @ConditionalOnMissingBean
    public CredStashTextEncryptor credStashTextEncryptor(
            @Value("${credstash.encryptor.secret.name}") String secret,
            @Value("${credstash.encryptor.secret.version:}") String version) {
        return new CredStashTextEncryptor(
                credStash(),
                new SecretRequest(secret)
                        .withTable(credStashProperties.getTable())
                        .withVersion(version)
        );
    }

    @Bean
    PropertySourceLocator secretPropertySources() {
        CompositePropertySource propertySource = new CompositePropertySource("secret_property_sources");
        for (CredStashPropertyConfig propertyConfig : credStashPropertyConfigs) {
            for (SecretPropertySourceConfig sourceConfig : propertyConfig.getSources()) {
                propertySource.addPropertySource(new SecretPropertySource(credStash(), propertyConfig, sourceConfig));
            }
        }
        return environment -> propertySource;
    }

}
