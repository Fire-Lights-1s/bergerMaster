package com.itwillbs.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.itwillbs.domain.manufacture.MFBomDTO;
import com.itwillbs.domain.manufacture.MFOrderDTO;
import com.itwillbs.entity.Item;
import com.itwillbs.entity.MFOrder;
import com.itwillbs.service.MFService;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Controller
@RequiredArgsConstructor
@Log
@RequestMapping("/mf")
public class MFController {
	
	private final MFService mfService;
	
	@GetMapping("/orders")
	public String orders(Model model) {
		log.info("MFController order()");
		
		List<MFOrderDTO> orderList = mfService.getOrderList();
		
		model.addAttribute("orderList", orderList);
		
		return "/manufacture/orders";
	}
	
	@GetMapping("/bom")
	public String bom(Model model) {
		log.info("MFController bom()");
		
		List<MFBomDTO> bomList = new ArrayList<>();
		
		List<Item> ppList = mfService.getPPList();
		
		for(int i = 0; i<ppList.size(); i++) {
			MFBomDTO bom = new MFBomDTO();
			
			bom.setItemCode(ppList.get(i).getItemCode());
			bom.setItemName(ppList.get(i).getItemName());
			
			bomList.add(bom);
		}
		
		bomList = mfService.getRmList(bomList);
		
		log.info(bomList.toString());
		
		model.addAttribute("bomList", bomList);
		
		return "/manufacture/BOM";
	}
	
	@GetMapping("/insert")
	public String insert() {
		
		return "/manufacture/orderInsert";
	}
}
