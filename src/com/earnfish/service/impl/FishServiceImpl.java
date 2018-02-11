package com.earnfish.service.impl;

import com.earnfish.annotion.Service;
import com.earnfish.service.FishService;

@Service("fishServiceImpl")
public class FishServiceImpl implements FishService {

	@Override
	public String get() {
		System.out.println("get one big shark!");
		return "shark";
	}

}
