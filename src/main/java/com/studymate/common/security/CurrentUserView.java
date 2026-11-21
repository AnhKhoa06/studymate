package com.studymate.common.security;

import java.io.Serial;
import java.io.Serializable;

public record CurrentUserView(String fullName, String role) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
