package com.SRHF.SRHF.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LogoutController {

    @GetMapping("/logout")
    public String logoutPage(Model model) {
        // simply return the logout confirmation view
        return "logout";
    }
}
