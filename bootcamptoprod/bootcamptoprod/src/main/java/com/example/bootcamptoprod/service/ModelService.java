package com.example.bootcamptoprod.service;

import com.example.bootcamptoprod.dto.OpenRouterModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ModelService {

    private static final Logger log = LoggerFactory.getLogger(ModelService.class);

    private final RestTemplate restTemplate;
    private final String apiKey;

    // OpenRouter base URL
    private static final String MODELS_URL = "https://openrouter.ai/api/v1/models";

    public ModelService(
            RestTemplateBuilder restTemplateBuilder,
            // env yoksa bile ":" sayesinde boş string gelir ve bean oluşur
            @Value("${spring.ai.openai.api-key:}") String apiKey
    ) {
        this.restTemplate = restTemplateBuilder.build();
        this.apiKey = apiKey;
    }

    public List<OpenRouterModel> getModels() {
        // API key hiç yoksa: servis patlamasın, boş liste dönsün
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("OpenRouter API key bulunamadı, model listesi boş döndürülecek.");
            return List.of();
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
            headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> resp = restTemplate.exchange(
                    MODELS_URL,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Object dataObj = resp.getBody().get("data");
                List<OpenRouterModel> result = new ArrayList<>();

                if (dataObj instanceof List<?> dataList) {
                    for (Object o : dataList) {
                        if (o instanceof Map<?, ?> m) {
                            String id = stringOrNull(m.get("id"));
                            // bazı modellerde "name" olmayabiliyor
                            String name = stringOrNull(m.get("name"));
                            String description = stringOrNull(m.get("description"));

                            // DTO’n senin aşağıdaki gibi 3 alanlıysa:
                            OpenRouterModel dto = new OpenRouterModel(id, name, description);
                            result.add(dto);
                        }
                    }
                }

                return result;
            } else {
                log.warn("OpenRouter modelleri 200 dönmedi: {}", resp.getStatusCode());
                return List.of();
            }

        } catch (RestClientException ex) {
            log.error("OpenRouter modelleri alınırken hata oluştu", ex);
            // burada da servis patlamasın
            return List.of();
        }
    }

    public String getApiKey() {
        return apiKey;
    }

    private String stringOrNull(Object o) {
        return o == null ? null : o.toString();
    }
}
