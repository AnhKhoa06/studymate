package com.studymate.common.web;

import java.io.Serializable;

public record MenuItem(
        String label,
        String url,
        String icon,
        boolean showOnDesktop,
        boolean showOnMobileBottom,
        boolean active
) implements Serializable {

    /** Tạo MenuItem chưa tính active (dùng trong MenuRegistry) */
    public MenuItem(String label, String url, String icon,
                    boolean showOnDesktop, boolean showOnMobileBottom) {
        this(label, url, icon, showOnDesktop, showOnMobileBottom, false);
    }

    /** Trả về bản sao có active được tính lại */
    public MenuItem withActive(boolean active) {
        return new MenuItem(label, url, icon, showOnDesktop, showOnMobileBottom, active);
    }
}
