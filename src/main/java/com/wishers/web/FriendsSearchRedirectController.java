package com.wishers.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Controller
public class FriendsSearchRedirectController {

    @GetMapping("/friends/search")
    public String search(@RequestParam(value = "q", required = false) String q) {
        if (q == null || q.isBlank()) {
            return "redirect:/friends";
        }
        String encoded = UriUtils.encodeQueryParam(q, StandardCharsets.UTF_8);
        return "redirect:/friends?q=" + encoded;
    }
}
