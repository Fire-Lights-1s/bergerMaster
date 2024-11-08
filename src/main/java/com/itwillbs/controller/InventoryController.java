package com.itwillbs.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.web.bind.annotation.RequestParam;

import com.itwillbs.domain.inventory.InventoryItemDTO;
import com.itwillbs.service.InventoryService;

@Controller
@RequestMapping("/inven")
@Log
@RequiredArgsConstructor
public class InventoryController {

	// 공통된 View 경로를 상수로 정의
	private static final String VIEW_PATH = "inventory_management/";

	private final InventoryService inventoryService;

//    재고 조회
	@GetMapping("/inventoryList")
	public String inventoryList(Model model, @PageableDefault(size = 10) Pageable pageable) {
		log.info("InventoryController inventoryList()");

		// 페이징 처리하여 재고 중 10개의 데이터만 저장
		Page<InventoryItemDTO> inventoryItemByPage = inventoryService.getInventoryItems(pageable);

		// 모델에 조회된 재고 데이터를 저장
		model.addAttribute("inventoryItemDTOs", inventoryItemByPage.getContent());
		model.addAttribute("pageSize", pageable.getPageSize());

		// 검색 조건의 기본값 설정
		model.addAttribute("itemCodeOrName", "");
		model.addAttribute("itemType", "");
		model.addAttribute("findOutOfStock", "N");

	    // 페이지네이션 범위 계산
	    int currentPage = inventoryItemByPage.getNumber() + 1; // 현재 페이지 (1부터 시작)
	    int totalPages = inventoryItemByPage.getTotalPages(); // 총 페이지 수

	    int startPage, endPage;

	    if (currentPage == 1) {
	        startPage = currentPage;
	        endPage = Math.min(totalPages, currentPage + 1);
	    } else if (currentPage == totalPages) {
	        startPage = Math.max(1, currentPage - 1);
	        endPage = currentPage;
	    } else {
	        startPage = currentPage - 1;
	        endPage = currentPage + 1;
	    }
	    
		model.addAttribute("currentPage", currentPage);
		model.addAttribute("totalPages", totalPages);
	    model.addAttribute("startPage", startPage);
	    model.addAttribute("endPage", endPage);
		
		return VIEW_PATH + "inventory_list";
	}

	// 재고 조회 검색
	@GetMapping("/inventoryListSearch")
	public String inventoryListSearch(Model model,
			@RequestParam(name = "itemCodeOrName", defaultValue = "") String itemCodeOrName,
			@RequestParam(name = "itemType", defaultValue = "") String itemType,
			@RequestParam(name = "findOutOfStock", defaultValue = "N") String findOutOfStock,
			@PageableDefault(size = 10) Pageable pageable) {

		log.info("InventoryController inventoryListSearch()");

		Page<InventoryItemDTO> inventoryItemByPage;

		boolean isFindOutOfStock = "Y".equals(findOutOfStock);

		if (isFindOutOfStock == true) {
			inventoryItemByPage = inventoryService.findInventoryItemsByOutOfStock(itemCodeOrName, itemType, pageable);
		} else {
			inventoryItemByPage = inventoryService.findInventoryItems(itemCodeOrName, itemType, pageable);
		}

		model.addAttribute("inventoryItemDTOs", inventoryItemByPage.getContent());
		model.addAttribute("pageSize", pageable.getPageSize());
		model.addAttribute("itemCodeOrName", itemCodeOrName);
		model.addAttribute("itemType", itemType);
		model.addAttribute("findOutOfStock", findOutOfStock);

	    // 페이지네이션 범위 계산
	    int currentPage = inventoryItemByPage.getNumber() + 1; // 현재 페이지 (1부터 시작)
	    int totalPages = inventoryItemByPage.getTotalPages(); // 총 페이지 수

	    int startPage, endPage;

	    // 현재페이지가 1일때, 끝 페이지 일때, 중간에 있을때로 구분
	    if (currentPage == 1) {
	        startPage = currentPage;
	        endPage = Math.min(totalPages, currentPage + 1);
	    } else if (currentPage == totalPages) {
	        startPage = Math.max(1, currentPage - 1);
	        endPage = currentPage;
	    } else {
	        startPage = currentPage - 1;
	        endPage = currentPage + 1;
	    }
	    
		model.addAttribute("currentPage", currentPage);
		model.addAttribute("totalPages", totalPages);
	    model.addAttribute("startPage", startPage);
	    model.addAttribute("endPage", endPage);


		return VIEW_PATH + "inventory_list";
	}

	// 입고 등록
	@GetMapping("/incomingInsert")
	public String incomingInsert() {
		log.info("InventroyController incomingInsert()");

		return VIEW_PATH + "incoming_insert";
	}

	// 입고 조회
	@GetMapping("/incomingList")
	public String incomingList(Model model, @PageableDefault(size = 10) Pageable pageable) {
		log.info("InventroyController incomingList()");

		
		//입고된 리스트 조회
//		Page<InventoryItemDTO> incomingByPage = inventoryService.getIncomingLists(pageable);
		
		
		//model에 저장
//		model.getAttribute()
		
		
		//페이징 처리
		
		
		
		return VIEW_PATH + "incoming_list";
	}

	// 출고 등록
	@GetMapping("/outgoingInsert")
	public String outgoingInsert() {
		log.info("InventroyController outgoingInsert()");

		return VIEW_PATH + "outgoing_insert";
	}

	// 출고 조회
	@GetMapping("/outgoingList")
	public String outgoingList(Model model, @PageableDefault(size = 10) Pageable pageable) {
		log.info("InventroyController outgoingList()");

		return VIEW_PATH + "outgoing_list";
	}

}
