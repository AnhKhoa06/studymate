package com.studymate.common.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LayoutModelAdviceTest {

    @Test
    @DisplayName("Path bằng url chính xác thì active")
    void testExactMatchIsActive() {
        assertThat(LayoutModelAdvice.isActive("/groups", "/groups")).isTrue();
        assertThat(LayoutModelAdvice.isActive("/matching", "/matching")).isTrue();
        assertThat(LayoutModelAdvice.isActive("/bookings", "/bookings")).isTrue();
        assertThat(LayoutModelAdvice.isActive("/chat", "/chat")).isTrue();
    }

    @Test
    @DisplayName("Path bắt đầu bằng url + '/' (trang con) thì active")
    void testSubPathIsActive() {
        assertThat(LayoutModelAdvice.isActive("/groups/5", "/groups")).isTrue();
        assertThat(LayoutModelAdvice.isActive("/groups/create", "/groups")).isTrue();
        assertThat(LayoutModelAdvice.isActive("/bookings/123/edit", "/bookings")).isTrue();
    }

    @Test
    @DisplayName("Path chỉ trùng tiền tố nhưng không có '/' ngăn cách thì KHÔNG active")
    void testPrefixWithoutSlashIsNotActive() {
        assertThat(LayoutModelAdvice.isActive("/groupsx", "/groups")).isFalse();
        assertThat(LayoutModelAdvice.isActive("/group", "/groups")).isFalse();
        assertThat(LayoutModelAdvice.isActive("/matching-now", "/matching")).isFalse();
    }

    @Test
    @DisplayName("'/' chỉ active khi đúng đường dẫn '/'")
    void testRootPathOnlyActivatesRootMenu() {
        assertThat(LayoutModelAdvice.isActive("/", "/groups")).isFalse();
        assertThat(LayoutModelAdvice.isActive("/", "/matching")).isFalse();
        assertThat(LayoutModelAdvice.isActive("/", "/")).isTrue();
        assertThat(LayoutModelAdvice.isActive("/groups", "/")).isFalse();
    }

    @Test
    @DisplayName("Path hoặc url là null thì an toàn trả false")
    void testNullSafety() {
        assertThat(LayoutModelAdvice.isActive(null, "/groups")).isFalse();
        assertThat(LayoutModelAdvice.isActive("/groups", null)).isFalse();
        assertThat(LayoutModelAdvice.isActive(null, null)).isFalse();
    }
}
