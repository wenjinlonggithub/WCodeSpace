package com.okx.finance.user.controller;

import com.okx.finance.common.dto.Result;
import com.okx.finance.user.dto.LoginRequest;
import com.okx.finance.user.dto.RegisterRequest;
import com.okx.finance.user.dto.KycRequest;
import com.okx.finance.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @PostMapping("/kyc")
    public Result<?> submitKyc(@RequestHeader("Authorization") String token,
                                @RequestBody KycRequest request) {
        return userService.submitKyc(token, request);
    }

    @GetMapping("/info")
    public Result<?> getUserInfo(@RequestHeader("Authorization") String token) {
        return userService.getUserInfo(token);
    }

    @PostMapping("/apikey/generate")
    public Result<?> generateApiKey(@RequestHeader("Authorization") String token) {
        return userService.generateApiKey(token);
    }
}
