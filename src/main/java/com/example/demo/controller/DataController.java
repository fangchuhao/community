package com.example.demo.controller;

import com.example.demo.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
public class DataController {
    @Autowired
    private DataService dataService;

    @RequestMapping(value = "/data",method = {RequestMethod.GET,RequestMethod.POST})
    public String date() {
        return "site/admin/data";
    }

    @RequestMapping(value = "/data/uv",method = {RequestMethod.POST})
    public String recordUV(@DateTimeFormat(pattern = "yyyy-MM-dd")Date start,
                           @DateTimeFormat(pattern = "yyyy-MM-dd")Date end,
                           Model model) {
        long countUV = dataService.calculateUV(start,end);
        model.addAttribute("countUV",countUV);
        model.addAttribute("uvStart",start);
        model.addAttribute("uvEnd",end);
        return "forward:/data";
    }

    @RequestMapping(value = "/data/dau",method = {RequestMethod.POST})
    public String recordDAU(@DateTimeFormat(pattern = "yyyy-MM-dd")Date start,
                           @DateTimeFormat(pattern = "yyyy-MM-dd")Date end,
                           Model model) {
        long countDAU = dataService.calculateDAU(start,end);
        model.addAttribute("countDAU",countDAU);
        model.addAttribute("dauStart",start);
        model.addAttribute("dauEnd",end);
        return "forward:/data";
    }
}
