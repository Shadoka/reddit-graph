package com.shadoka.graph.reddit;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("reddit_credentials.properties")
public class RedditConfig {
}
