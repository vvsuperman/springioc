package com.earnfish.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.earnfish.annotion.Controller;
import com.earnfish.annotion.Qualifier;
import com.earnfish.annotion.RequestMapping;
import com.earnfish.service.FishService;

@Controller("fish")
public class FishController {
	
	@Qualifier("fishServiceImpl")
	FishService fishservice;
	
	@RequestMapping("get")
	public String getFish(HttpServletRequest req, HttpServletResponse rsp, String param) {
		return fishservice.get();
	}

}
