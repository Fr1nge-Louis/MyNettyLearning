package cn.lhs.websocket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
@Controller
@RequestMapping("/main")
public class LoginController {
    @RequestMapping("/page")
    public String test(){
        return "../static/pages/home";
    }

}
