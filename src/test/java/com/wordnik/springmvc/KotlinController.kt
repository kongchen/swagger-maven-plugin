package com.wordnik.springmvc

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class KotlinController {

    @GetMapping("/getWithParam")
    fun getWithParam(@RequestParam optionalParam: Boolean = false) {
    }
}
