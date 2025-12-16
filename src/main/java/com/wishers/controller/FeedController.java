package com.wishers.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FeedController {

    @GetMapping("/feed")
    public String feed(Principal principal, Model model) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "feed/index";
    }
}
