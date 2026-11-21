package com.studymate.common.web;

import java.io.Serializable;

public record MenuItem(
        String label,
        String url,
        String icon,
        boolean showOnDesktop,
        boolean showOnMobileBottom
) implements Serializable {
}
