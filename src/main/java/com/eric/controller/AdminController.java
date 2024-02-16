package com.eric.controller;

import com.eric.domain.*;
import com.eric.mail.MailConfig;
import com.eric.mail.MailUtils;
import com.eric.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.mail.MessagingException;


@Controller
public class AdminController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private MailConfig mailConfig;

    @GetMapping("/admin")
    public String quoteList(Model model) {
        SyncResult result = new SyncResult();
        model.addAttribute("result", result);
        return "admin";
    }

    @PostMapping("/truncateQuote")
    public String truncateQuote(Model model) {
        SyncResult result = new SyncResult();
        try {
            adminService.truncateQuote();
            result.setMsg("已清空報價資料庫");
            model.addAttribute("result", result);
        } catch (Exception e) {
            result.setMsg(String.format("報價資料庫清空失敗 %s", e.getMessage()));
            model.addAttribute("result", result);
        }

        return "admin";
    }

    @PostMapping("/truncateSymbol")
    public String truncateSymbol(Model model) {
        SyncResult result = new SyncResult();
        try {
            adminService.truncateSymbol();
            result.setMsg("已清空標的資料庫");
            model.addAttribute("result", result);
        } catch (Exception e) {
            result.setMsg(String.format("標的資料庫清空失敗 %s", e.getMessage()));
            model.addAttribute("result", result);
        }

        return "admin";
    }

    @PostMapping("/test")
    public String test(Model model) {
        SyncResult result = new SyncResult();

        String[] to = new String[]{"sender@gmail.com"};

        try {

            MailUtils.generateAndSendEmail(mailConfig, to, "test", "test", "test.txt", null);
            result.setMsg("mail success");
            model.addAttribute("result", result);
        } catch (MessagingException e) {
            result.setMsg(String.format("mail failed %s", e.getMessage()));
            model.addAttribute("result", result);
        }

        return "admin";
    }


}
