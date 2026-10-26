package com.javaee.documentsystem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class AiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 留空即视为"未配置"，AI 摘要会直接给出提示，不会影响服务和其它功能 */
    @Value("${ai.deepseek.api-key:}")
    private String apiKey;

    @Value("${ai.deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${ai.deepseek.model:deepseek-chat}")
    private String model;

    public AiService() {
        // 加超时，避免 DeepSeek 卡住时把 Tomcat 线程一起拖住
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(30));
        this.restTemplate = new RestTemplate(factory);
    }

    public String generateSummary(String content) {
        if (isBlank(apiKey)) {
            return "未配置 DeepSeek API Key（ai.deepseek.api-key），AI 摘要暂不可用；其它功能不受影响";
        }
        if (isBlank(content)) {
            return "内容为空，无法生成摘要";
        }

        String url = baseUrl.replaceAll("/+$", "") + "/chat/completions";

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", new Object[]{
                Map.of("role", "user", "content", "请用中文总结以下内容，50字以内：" + content)
        });
        requestBody.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey.trim());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            System.out.println("正在调用 DeepSeek API...");
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );
            System.out.println("API 响应状态码：" + response.getStatusCode());

            if (response.getStatusCode() != HttpStatus.OK) {
                return "API 调用失败，状态码：" + response.getStatusCode();
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode node = root.path("choices").path(0).path("message").path("content");
            if (node.isMissingNode() || node.isNull()) {
                return "摘要生成失败：返回内容里没有 choices[0].message.content";
            }
            return node.asText();
        } catch (HttpClientErrorException e) {
            // key 被删/失效时走这里：401 或 403，给出人话提示而不是一堆堆栈
            int code = e.getStatusCode().value();
            if (code == 401 || code == 403) {
                System.err.println("DeepSeek 鉴权失败（" + code + "）：请检查 ai.deepseek.api-key 是否有效");
                return "API Key 无效或已被删除（HTTP " + code + "），请到平台重新生成并填入 ai.deepseek.api-key";
            }
            return "API 调用失败（HTTP " + code + "）：" + e.getResponseBodyAsString();
        } catch (Exception e) {
            e.printStackTrace();
            return "摘要生成失败：" + e.getMessage();
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
