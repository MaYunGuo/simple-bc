package com.simple.bc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WolcomeController {

    @RequestMapping(value={"/","data.html"})
    public String welcome(){
        return "data";
    }
}
