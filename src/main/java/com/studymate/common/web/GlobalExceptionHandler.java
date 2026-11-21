package com.studymate.common.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

// Xu ly loi chung: khong hien stack trace, khong hien ten class hay duong dan noi bo
@ControllerAdvice
public class GlobalExceptionHandler {

    // 403: khong co quyen
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleAccessDenied(HttpServletRequest request, AccessDeniedException ex) {
        return new ModelAndView("error/403");
    }

    // 500: loi he thong khong xu ly duoc
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGeneral(HttpServletRequest request, Exception ex) {
        // Ghi log noi bo de debug, khong tra chi tiet ra client
        return new ModelAndView("error/500");
    }
}
