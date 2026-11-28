package com.studymate.user.service;

import com.studymate.user.dto.AiHistoryDto;
import com.studymate.user.dto.AssistantResponseDto;
import com.studymate.user.entity.AiRequest;
import com.studymate.user.entity.AiRequestType;
import com.studymate.user.repository.AiRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AssistantService {

    private static final Logger log = LoggerFactory.getLogger(AssistantService.class);
    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={apiKey}";

    private final AiRequestRepository aiRequestRepository;
    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final int rateLimitPerHour;

    public AssistantService(
            AiRequestRepository aiRequestRepository,
            @Value("${ai.llm.api-key:}") String apiKey,
            @Value("${ai.llm.model:gemini-3.5-flash-lite}") String model,
            @Value("${ai.llm.rate-limit-per-hour:20}") int rateLimitPerHour) {
        this.aiRequestRepository = aiRequestRepository;
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.model = model;
        this.rateLimitPerHour = rateLimitPerHour;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Transactional
    public AssistantResponseDto askStudyAssistant(Long userId, String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập câu hỏi cho trợ lý AI");
        }
        String cleanQuestion = question.trim();
        String prompt = "Bạn là trợ lý học tập StudyMate cho sinh viên đại học Việt Nam. "
                + "Hãy trả lời ngắn gọn, thực tế bằng tiếng Việt cho câu hỏi sau:\n" + cleanQuestion;

        return executeWithGuard(
                userId,
                AiRequestType.STUDY_QA,
                summarize(cleanQuestion),
                prompt,
                () -> buildStudyQaFallback(cleanQuestion)
        );
    }

    // Hop dong cho module Matching (Khuong): giai thich vi sao hai ban hop nhau
    @Transactional
    public AssistantResponseDto explainMatch(Long userId,
                                             String studentA,
                                             String studentB,
                                             int score,
                                             String criteriaSummary) {
        String safeA = (studentA == null || studentA.isBlank()) ? "Bạn" : studentA.trim();
        String safeB = (studentB == null || studentB.isBlank()) ? "bạn học" : studentB.trim();
        String safeCriteria = (criteriaSummary == null || criteriaSummary.isBlank())
                ? "môn học chung, khung giờ rảnh và mục tiêu học tập"
                : criteriaSummary.trim();

        String prompt = "Hãy giải thích ngắn gọn (3-4 câu, tiếng Việt, thân thiện) vì sao "
                + safeA + " và " + safeB + " phù hợp học nhóm với điểm " + score + "/100. "
                + "Chi tiết tiêu chí: " + safeCriteria;

        return executeWithGuard(
                userId,
                AiRequestType.MATCH_EXPLAIN,
                summarize("Giải thích ghép đôi " + safeA + " - " + safeB + " (" + score + "/100)"),
                prompt,
                () -> buildMatchFallback(safeA, safeB, score, safeCriteria)
        );
    }

    // Hop dong cho module Nhom/Chat (Huy): tom tat chat nhom thanh checklist buoi hoc
    @Transactional
    public AssistantResponseDto summarizeChatToChecklist(Long userId,
                                                         String groupName,
                                                         List<String> messages) {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("Chưa có nội dung tin nhắn để tóm tắt");
        }
        String safeGroup = (groupName == null || groupName.isBlank()) ? "Nhóm học" : groupName.trim();
        String joined = String.join("\n", messages.stream()
                .filter(m -> m != null && !m.isBlank())
                .limit(40)
                .toList());

        String prompt = "Hãy tóm tắt đoạn trao đổi của nhóm '" + safeGroup + "' thành checklist việc cần chuẩn bị "
                + "cho buổi học sắp tới (dạng gạch đầu dòng '- [ ] ...', tối đa 6 mục, tiếng Việt):\n" + joined;

        return executeWithGuard(
                userId,
                AiRequestType.CHAT_CHECKLIST,
                summarize("Tóm tắt chat nhóm " + safeGroup),
                prompt,
                () -> buildChecklistFallback(safeGroup, messages)
        );
    }

    @Transactional(readOnly = true)
    public List<AiHistoryDto> getRecentHistory(Long userId) {
        return aiRequestRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(req -> new AiHistoryDto(
                        req.getId(),
                        req.getRequestType(),
                        req.getPromptSummary(),
                        req.getResponseText(),
                        req.isUsedFallback(),
                        req.getCreatedAt()
                ))
                .toList();
    }

    private AssistantResponseDto executeWithGuard(Long userId,
                                                AiRequestType type,
                                                String promptSummary,
                                                String prompt,
                                                java.util.function.Supplier<String> fallbackSupplier) {
        LocalDateTime now = LocalDateTime.now();
        long recentCount = aiRequestRepository.countByUserIdAndCreatedAtAfter(userId, now.minusHours(1));
        boolean rateLimited = recentCount >= rateLimitPerHour;

        String answer = null;
        boolean fallbackUsed = false;

        if (rateLimited) {
            answer = "[Đã đạt giới hạn " + rateLimitPerHour + " lượt/giờ — dùng gợi ý dự phòng]\n"
                    + fallbackSupplier.get();
            fallbackUsed = true;
        } else {
            answer = tryCallLlm(prompt);
            if (answer == null || answer.isBlank()) {
                answer = fallbackSupplier.get();
                fallbackUsed = true;
            }
        }

        AiRequest record = new AiRequest();
        record.setUserId(userId);
        record.setRequestType(type);
        record.setPromptSummary(promptSummary);
        record.setResponseText(answer);
        record.setUsedFallback(fallbackUsed);
        record.setCreatedAt(now);
        aiRequestRepository.save(record);

        return new AssistantResponseDto(type, answer, fallbackUsed, rateLimited, now);
    }

    @SuppressWarnings("unchecked")
    protected String tryCallLlm(String prompt) {
        if (apiKey.isEmpty()) {
            return null;
        }
        try {
            Map<String, Object> body = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", prompt)))
                    )
            );

            Map<String, Object> response = restClient.post()
                    .uri(GEMINI_API_URL, model, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                return null;
            }
            Object candidatesObj = response.get("candidates");
            if (!(candidatesObj instanceof List<?> candidates) || candidates.isEmpty()) {
                return null;
            }
            Object first = candidates.get(0);
            if (!(first instanceof Map<?, ?> candidateMap)) {
                return null;
            }
            Object contentObj = candidateMap.get("content");
            if (!(contentObj instanceof Map<?, ?> contentMap)) {
                return null;
            }
            Object partsObj = contentMap.get("parts");
            if (!(partsObj instanceof List<?> parts) || parts.isEmpty()) {
                return null;
            }
            Object firstPart = parts.get(0);
            if (firstPart instanceof Map<?, ?> partMap && partMap.get("text") instanceof String text) {
                return text.trim();
            }
            return null;
        } catch (Exception ex) {
            // Khong in API key hay chi tiet nhay cam ra log
            log.warn("Goi API LLM that bai, chuyen sang phuong an du phong: {}", ex.getClass().getSimpleName());
            return null;
        }
    }

    private String buildStudyQaFallback(String question) {
        return "Gợi ý nhanh (chế độ dự phòng):\n"
                + "- Mục tiêu trọng tâm: Chia nội dung \"" + summarize(question) + "\" thành 2-3 phần nhỏ theo buổi.\n"
                + "- Trước buổi học: Mỗi thành viên đọc trước tài liệu 20 phút và ghi 2 câu hỏi còn vướng.\n"
                + "- Trong buổi học: Dành 15 phút đầu thống nhất đầu mục, 45 phút giải bài tập/code chung và 10 phút chốt việc tiếp theo.";
    }

    private String buildMatchFallback(String studentA, String studentB, int score, String criteriaSummary) {
        String levelText = score >= 80 ? "rất phù hợp" : (score >= 60 ? "khá phù hợp" : "có điểm chung cơ bản");
        return studentA + " và " + studentB + " đạt mức độ phù hợp " + score + "/100 (" + levelText + "). "
                + "Điểm nổi bật dựa trên: " + criteriaSummary + ". "
                + "Hai bạn nên thống nhất trước một khung giờ rảnh cố định trong tuần và đặt phòng học nhóm để bắt đầu.";
    }

    private String buildChecklistFallback(String groupName, List<String> messages) {
        StringBuilder sb = new StringBuilder("Checklist chuẩn bị buổi học cho ").append(groupName).append(":\n");
        int count = 0;
        for (String msg : messages) {
            if (msg == null || msg.isBlank()) {
                continue;
            }
            sb.append("- [ ] ").append(summarize(msg)).append("\n");
            count++;
            if (count >= 5) {
                break;
            }
        }
        sb.append("- [ ] Xác nhận lại giờ họp và phòng học trước khi bắt đầu.");
        return sb.toString();
    }

    private String summarize(String text) {
        String trimmed = text.replaceAll("\\s+", " ").trim();
        return trimmed.length() <= 180 ? trimmed : trimmed.substring(0, 177) + "...";
    }
}
