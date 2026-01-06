package com.architecture.middleware.logging;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ElasticsearchExample {

    @Autowired(required = false)
    private ElasticsearchClient elasticsearchClient;

    public void indexLog(String level, String message) {
        try {
            Map<String, Object> logData = new HashMap<>();
            logData.put("timestamp", LocalDateTime.now().toString());
            logData.put("level", level);
            logData.put("message", message);
            logData.put("application", "middleware-demo");

            IndexRequest<Map<String, Object>> request = IndexRequest.of(i -> i
                .index("application-logs")
                .document(logData)
            );

            elasticsearchClient.index(request);
            System.out.println("Log indexed to Elasticsearch: " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void searchLogs(String query) {
        try {
            SearchRequest request = SearchRequest.of(s -> s
                .index("application-logs")
                .query(q -> q
                    .match(t -> t
                        .field("message")
                        .query(query)
                    )
                )
            );

            SearchResponse<Map> response = elasticsearchClient.search(request, Map.class);
            
            System.out.println("Found " + response.hits().total().value() + " logs for query: " + query);
            response.hits().hits().forEach(hit -> {
                System.out.println("Log: " + hit.source());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}