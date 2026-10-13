package com.javaee.documentsystem.controller;

import com.javaee.documentsystem.service.AiService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/summary")
    public Map<String, String> generateSummary(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        String summary = aiService.generateSummary(content);

        Map<String, String> response = new HashMap<>();
        response.put("summary", summary);
        System.out.println("收到AI请求，内容：" + request.get("content"));
        return response;
    }
}