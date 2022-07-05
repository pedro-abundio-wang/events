package com.events.core.messaging.publisher;

import com.events.core.messaging.config.MessagePublisherConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({MessagePublisherConfiguration.class})
public class MessagePublisherTestConfiguration {}
