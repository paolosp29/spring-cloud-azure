/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.cache;

import com.microsoft.azure.management.redis.RedisAccessKeys;
import com.microsoft.azure.management.redis.RedisCache;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AzureRedisAutoConfigurationTest {
    private static final String KEY = "KEY";
    private static final String HOST = "localhost";
    private static final int PORT = 6379;
    private static final boolean IS_SSL = true;
    private ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(AzureRedisAutoConfiguration.class))
                                          .withUserConfiguration(TestConfiguration.class);

    @Test
    public void testAzureRedisDisabled() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.redis.enabled=false")
                          .run(context -> assertThat(context).doesNotHaveBean(AzureRedisProperties.class));
    }

    @Test
    public void testWithoutRedisOperationsClass() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(RedisOperations.class))
                          .run(context -> assertThat(context).doesNotHaveBean(AzureRedisProperties.class));
    }

    @Test
    public void testAzureRedisPropertiesConfigured() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.redis.name=redis").run(context -> {
            assertThat(context).hasSingleBean(AzureRedisProperties.class);
            assertThat(context.getBean(AzureRedisProperties.class).getName()).isEqualTo("redis");
            assertThat(context).hasSingleBean(RedisProperties.class);
            assertThat(context.getBean(RedisProperties.class).getPassword()).isEqualTo(KEY);
            assertThat(context.getBean(RedisProperties.class).getHost()).isEqualTo(HOST);
            assertThat(context.getBean(RedisProperties.class).getPort()).isEqualTo(PORT);
            assertThat(context.getBean(RedisProperties.class).isSsl()).isEqualTo(IS_SSL);
        });
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        AzureAdmin azureAdmin() {

            AzureAdmin azureAdmin = mock(AzureAdmin.class);
            RedisCache redisCache = mock(RedisCache.class);
            RedisAccessKeys accessKeys = mock(RedisAccessKeys.class);
            when(accessKeys.primaryKey()).thenReturn(KEY);
            when(redisCache.hostName()).thenReturn(HOST);
            when(redisCache.nonSslPort()).thenReturn(!IS_SSL);
            when(redisCache.sslPort()).thenReturn(PORT);
            when(redisCache.shardCount()).thenReturn(0);
            when(redisCache.getKeys()).thenReturn(accessKeys);
            when(azureAdmin.getOrCreateRedisCache(isA(String.class))).thenReturn(redisCache);
            return azureAdmin;
        }

    }
}