package de.yolacraft.lcqbot.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {

    @GetMapping("/debug")
    public String debug() {
        System.out.println("DEBUG HIT");
        return "ok";
    }
}