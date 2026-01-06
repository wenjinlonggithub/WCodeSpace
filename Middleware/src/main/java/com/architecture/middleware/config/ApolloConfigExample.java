package com.architecture.middleware.config;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.model.ConfigChangeListener;
import org.springframework.stereotype.Service;

@Service
public class ApolloConfigExample {

    private Config config;

    public void initApolloConfig() {
        config = ConfigService.getAppConfig();
        addChangeListener();
    }

    public String getProperty(String key, String defaultValue) {
        String value = config.getProperty(key, defaultValue);
        System.out.println("Apollo config - " + key + ": " + value);
        return value;
    }

    public int getIntProperty(String key, int defaultValue) {
        int value = config.getIntProperty(key, defaultValue);
        System.out.println("Apollo config - " + key + ": " + value);
        return value;
    }

    public boolean getBooleanProperty(String key, boolean defaultValue) {
        boolean value = config.getBooleanProperty(key, defaultValue);
        System.out.println("Apollo config - " + key + ": " + value);
        return value;
    }

    private void addChangeListener() {
        config.addChangeListener(new ConfigChangeListener() {
            @Override
            public void onChange(ConfigChangeEvent changeEvent) {
                System.out.println("Apollo config changed:");
                changeEvent.changedKeys().forEach(key -> {
                    System.out.println("Key: " + key + 
                        ", Old Value: " + changeEvent.getChange(key).getOldValue() +
                        ", New Value: " + changeEvent.getChange(key).getNewValue());
                });
            }
        });
    }
}