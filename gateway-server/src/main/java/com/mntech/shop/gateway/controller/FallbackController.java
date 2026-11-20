package com.mntech.shop.gateway.controller;


import com.mntech.shop.gateway.dto.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/fallback")
public class FallbackController {

    /**
     * 订单服务降级
     */
    @RequestMapping("/order")
    public Mono<Result<String>> orderFallback() {
        return Mono.just(Result.error("fallback系统繁忙，请稍后重试!"));
    }
}