package com.studymate.user.controller;

import com.studymate.common.security.SecurityUtils;
import com.studymate.user.dto.AssistantResponseDto;
import com.studymate.user.service.AssistantService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/assistant")
public class AssistantController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @GetMapping
    public String viewAssistant(Model model) {
        Long userId = requireCurrentUserId();
        model.addAttribute("history", assistantService.getRecentHistory(userId));
        return "user/assistant";
    }

    @PostMapping("/ask")
    public String askAssistant(@RequestParam("question") String question,
                               RedirectAttributes redirectAttributes) {
        Long userId = requireCurrentUserId();
        try {
            AssistantResponseDto response = assistantService.askStudyAssistant(userId, question);
            redirectAttributes.addFlashAttribute("latestUserPrompt", question != null ? question.trim() : "");
            redirectAttributes.addFlashAttribute("latestResponse", response);
            if (response.rateLimited()) {
                redirectAttributes.addFlashAttribute("infoMessage", "Bạn đã dùng hết lượt AI trong giờ, đang hiển thị gợi ý dự phòng.");
            }
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/assistant";
    }

    @PostMapping("/checklist")
    public String summarizeChecklist(@RequestParam(name = "groupName", defaultValue = "Nhóm học") String groupName,
                                     @RequestParam("chatContent") String chatContent,
                                     RedirectAttributes redirectAttributes) {
        Long userId = requireCurrentUserId();
        List<String> lines = chatContent == null
                ? List.of()
                : Arrays.stream(chatContent.split("\\r?\\n"))
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .toList();
        try {
            AssistantResponseDto response = assistantService.summarizeChatToChecklist(userId, groupName, lines);
            String promptDisplay = (groupName != null && !groupName.isBlank())
                    ? "[" + groupName.trim() + "]\n" + String.join("\n", lines)
                    : String.join("\n", lines);
            redirectAttributes.addFlashAttribute("latestUserPrompt", promptDisplay);
            redirectAttributes.addFlashAttribute("latestResponse", response);
            if (response.rateLimited()) {
                redirectAttributes.addFlashAttribute("infoMessage", "Bạn đã dùng hết lượt AI trong giờ, đang hiển thị checklist dự phòng.");
            }
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/assistant";
    }

    private Long requireCurrentUserId() {
        return SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException("Chưa đăng nhập"));
    }
}
