package com.fpt.careermate.config;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.v1.auth.exception.AuthException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.weaviate.client.WeaviateAuthClient;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WeaviateConfig {

    @Value("${weaviate.url}")
    private String weaviateUrl;

    @Value("${weaviate.api-key}")
    private String weaviateApiKey;

    @Bean
    public WeaviateClient weaviateClient() throws AuthException {
//        Config config = new Config("http", "localhost:8081"); // Local Weaviate instance
        String clusterUrl = "https://oei76mp3ttcpw5prggx3fq.c0.asia-southeast1.gcp.weaviate.cloud";

        Map<String, String> headers = new HashMap<>();
        headers.put("X-Weaviate-Cluster-URL", clusterUrl);

        Config config = new Config("https", weaviateUrl, headers);
        return WeaviateAuthClient.apiKey(config, weaviateApiKey);
    }
}
