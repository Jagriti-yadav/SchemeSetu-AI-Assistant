
package com.mahi.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Welcome to AI-Powered Government Scheme Management System!";
    }

    @GetMapping("/api/status")
    public String status() {
        return "Government Scheme RAG System is Running Successfully!";
    }
}

