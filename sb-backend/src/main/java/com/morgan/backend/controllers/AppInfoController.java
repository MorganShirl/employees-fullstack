package com.morgan.backend.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/info")
public class AppInfoController {

    public record RequestThreadInfoDto(boolean virtual) {}

    @GetMapping("/request-thread")
    public RequestThreadInfoDto requestThreadInfo() {
        Thread thread = Thread.currentThread();
        boolean isVirtual = thread.isVirtual();
        log.info("Request /request-thread: thread [{}] isVirtual? {}", thread, isVirtual);
        return new RequestThreadInfoDto(isVirtual);
    }
}
