package com.aric.middleware.controller;

import com.aric.middleware.annotation.WhiteListAnnotation;
import com.aric.middleware.common.Result;
import com.aric.middleware.configuration.WhiteListConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MainController {
    @Autowired
    private WhiteListConfiguration whiteListConfiguration;

    @GetMapping("/hello")
    @WhiteListAnnotation(key = "userId")
    public Result hello(@RequestParam("userId") String userId) {
        return Result.success("userId=" + userId + ":" + whiteListConfiguration.getUsers() + ":" + whiteListConfiguration.getIsOpen());
    }

    @GetMapping("/world")
    public Result world() {
        return Result.success("world");
    }
}
