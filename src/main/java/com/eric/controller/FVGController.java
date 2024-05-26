package com.eric.controller;

import com.eric.domain.SyncResult;
import com.eric.persist.pojo.FVGRecordDto;
import com.eric.service.FVGService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
public class FVGController {

    @Autowired
    private FVGService fvgService;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @GetMapping("/fvg")
    public String init(Model model) {
        this.setDefaultModel(model);
        return "fvg";
    }

    @GetMapping("/fvg/query")
    public String getFvgResult(Model model, @RequestParam(required = false, name = "queryDate") String startDateStr
            , @RequestParam(required = false, name = "endDate") String endDateStr) throws ParseException {
        this.setDefaultModel(model);
        SyncResult result = (SyncResult) model.getAttribute("result");

        if (StringUtils.isBlank(startDateStr) || StringUtils.isBlank(endDateStr)) {

            this.setDefaultModel(model);
            result.setMsg("日期起訖日資料錯誤");
            return "fvg";
        }
        LocalDate startDate = LocalDate.parse(startDateStr, dateFormatter);
        LocalDate endDate = LocalDate.parse(endDateStr, dateFormatter);

        List<FVGRecordDto> list = fvgService.findRecordByRange(startDate, endDate);
        //TODO 應該計算現價差距漲幅
        model.addAttribute("fvgs", list);
        return "fvg";
    }

    private void setDefaultModel(Model model) {
        SyncResult result = new SyncResult();
        model.addAttribute("result", result);
        model.addAttribute("fvgs", new ArrayList<>());
    }
}
