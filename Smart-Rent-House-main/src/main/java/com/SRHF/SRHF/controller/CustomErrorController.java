package com.SRHF.SRHF.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.RequestDispatcher;

@ControllerAdvice
public class CustomErrorController {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomErrorController.class);

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public String handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex, RedirectAttributes redirectAttributes) {
        logger.warn("File upload size exceeded: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", 
            "File size is too large. Maximum allowed size is 100MB. Please try with a smaller file.");
        return "redirect:/profile";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, RedirectAttributes redirectAttributes) {
        logger.error("Unexpected error occurred", ex);
        redirectAttributes.addFlashAttribute("error", "An unexpected error occurred. Please try again.");
        return "redirect:/profile";
    }

    /**
     * Handle 413 errors that occur during request parsing (before reaching controller)
     * These bypass the @ExceptionHandler above, so we need this explicit mapping
     */
    @GetMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            logger.warn("HTTP Error - Status Code: {}", statusCode);
            
            if (statusCode == HttpStatus.PAYLOAD_TOO_LARGE.value()) {
                logger.warn("413 Payload Too Large error - rendering error page");
                return "error";
            }
        }
        
        return "error";
    }

}
