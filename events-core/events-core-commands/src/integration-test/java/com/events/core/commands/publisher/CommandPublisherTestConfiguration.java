package com.events.core.commands.publisher;

import com.events.core.commands.spring.config.CommandPublisherConfiguration;
import com.events.core.messaging.config.MessagePublisherConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({CommandPublisherConfiguration.class, MessagePublisherConfiguration.class})
public class CommandPublisherTestConfiguration {}
