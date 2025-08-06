package org.example.aigeneration.controller;

import org.example.aigeneration.common.BaseResponse;
import org.example.aigeneration.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class testController{

    @GetMapping("/")
    public BaseResponse<String> test(){
        return ResultUtils.success("OK");
    }
}
