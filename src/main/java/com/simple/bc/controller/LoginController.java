package com.simple.bc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
public class LoginController {


    @RequestMapping("/login.do")
    public ModelAndView login(){
        return  new ModelAndView("login");
    }
    @RequestMapping("/logout.do")
    public String logout(){
       return null;
    }

    @RequestMapping("/editPwd.do")
    @ResponseBody
    public boolean updatePassword(HttpServletRequest request, HttpServletResponse response){
       return false;
    }
}
