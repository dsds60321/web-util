package com.tablegen.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @Value("${app.title}")
    private String appTitle;

    @Value("${app.logo}")
    private String appLogo;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", appTitle);
        model.addAttribute("logo", appLogo);
        return "index";
    }
}
