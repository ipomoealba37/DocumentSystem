package com.javaee.documentsystem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateSummary(String content) {
        String url = "https://api.deepseek.com/chat/completions";
        // 替换成你自己的 API Key
        String apiKey = "REMOVED-ROTATED-KEY";

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-chat");
        requestBody.put("messages", new Object[]{
                Map.of("role", "user", "content", "请用中文总结以下内容，50字以内：" + content)
        });
        // 可选：设置 temperature 等参数
        requestBody.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

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
            System.out.println("API 响应内容：" + response.getBody());

            if (response.getStatusCode() != HttpStatus.OK) {
                return "API 调用失败，状态码：" + response.getStatusCode();
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            String summary = root.path("choices").get(0).path("message").path("content").asText();
            return summary;
        } catch (Exception e) {
            // 打印完整异常栈
            e.printStackTrace();
            return "摘要生成失败：" + e.getMessage();
        }
    }
}