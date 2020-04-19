package com.sovreign.gateway.service.proxy

import org.springframework.cloud.netflix.ribbon.RibbonClient
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.*

@FeignClient(name = "authentication-service")
@RibbonClient(name = "authentication-service")
interface AuthFilterServiceProxy {
    @GetMapping("/authenticationService/session/checkAuthentication/{username}")
    fun checkAuthentication(@PathVariable("username") username: String, @RequestParam auth:String): Boolean

    @GetMapping("/authenticationService/account/test")
    fun test(): Int
}