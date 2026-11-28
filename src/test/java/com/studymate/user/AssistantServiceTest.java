package com.studymate.user;

import com.studymate.common.security.CurrentUser;
import com.studymate.user.dto.AssistantResponseDto;
import com.studymate.user.entity.AiRequest;
import com.studymate.user.entity.AiRequestType;
import com.studymate.user.entity.Role;
import com.studymate.user.entity.User;
import com.studymate.user.repository.AiRequestRepository;
import com.studymate.user.repository.UserRepository;
import com.studymate.user.service.AssistantService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AssistantServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AssistantService assistantService;

    @Autowired
    private AiRequestRepository aiRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        aiRequestRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void assistantEndpoint_requiresAuthenticationAndHandlesFallbackGracefully() throws Exception {
        mockMvc.perform(get("/assistant"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        User student = createStudent("ai.student@studymate.test");
        CurrentUser principal = toPrincipal(student);

        mockMvc.perform(get("/assistant").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("history"));

        mockMvc.perform(post("/assistant/ask")
                        .with(user(principal))
                        .with(csrf())
                        .param("question", "Lập kế hoạch ôn tập OOP 2 giờ"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/assistant"))
                .andExpect(flash().attributeExists("latestResponse"));

        assertThat(aiRequestRepository.findTop10ByUserIdOrderByCreatedAtDesc(student.getId())).hasSize(1);
    }

    @Test
    void contractMethods_explainMatchAndSummarizeChecklist_workWithFallback() {
        User student = createStudent("ai.contract@studymate.test");

        AssistantResponseDto matchRes = assistantService.explainMatch(
                student.getId(), "An", "Bình", 86, "Chung môn INT1434, rảnh sáng thứ 3");
        assertThat(matchRes.type()).isEqualTo(AiRequestType.MATCH_EXPLAIN);
        assertThat(matchRes.answer()).contains("86/100");

        AssistantResponseDto chatRes = assistantService.summarizeChatToChecklist(
                student.getId(), "Nhóm OOP", List.of("An vẽ class diagram", "Bình viết unit test"));
        assertThat(chatRes.type()).isEqualTo(AiRequestType.CHAT_CHECKLIST);
        assertThat(chatRes.answer()).contains("- [ ]");
    }

    @Test
    void rateLimit_enforcedAfterThresholdWithoutCrashing() {
        User student = createStudent("ai.ratelimit@studymate.test");
        for (int i = 0; i < 20; i++) {
            AiRequest req = new AiRequest();
            req.setUserId(student.getId());
            req.setRequestType(AiRequestType.STUDY_QA);
            req.setPromptSummary("Q" + i);
            req.setResponseText("A" + i);
            req.setUsedFallback(true);
            req.setCreatedAt(LocalDateTime.now().minusMinutes(5));
            aiRequestRepository.save(req);
        }

        AssistantResponseDto limited = assistantService.askStudyAssistant(student.getId(), "Hỏi vượt ngưỡng");
        assertThat(limited.rateLimited()).isTrue();
        assertThat(limited.fallbackUsed()).isTrue();
        assertThat(limited.answer()).contains("giới hạn");
    }

    private User createStudent(String email) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode("Secret123!"));
        u.setFullName("AI Student");
        u.setRole(Role.STUDENT);
        u.setEnabled(true);
        u.setCreatedAt(LocalDateTime.now());
        return userRepository.save(u);
    }

    private CurrentUser toPrincipal(User u) {
        return new CurrentUser(
                u.getId(), u.getEmail(), u.getPasswordHash(), u.getFullName(),
                u.getRole(), u.isEnabled(),
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()))
        );
    }
}
