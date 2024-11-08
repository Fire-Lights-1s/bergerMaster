package com.itwillbs.service;

import com.itwillbs.domain.transaction.OrderDTO;
import com.itwillbs.domain.transaction.OrderItemsDTO;
import com.itwillbs.entity.*;
import com.itwillbs.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Log
@RequiredArgsConstructor
@Service
public class TXService {

    private final OrderRepository orderRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final ManagerRepository managerRepository;
    private final SupplierRepository supplierRepository;
    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional
    public void saveOrder(OrderDTO orderDTO, List<OrderItemsDTO> orderItems) {
        log.info("TXService: saveOrder");
        // 발주번호 생성
        String orderId = generateNextOrderId();

        // 발주 정보 저장
        Order order = new Order();
        BeanUtils.copyProperties(orderDTO, order);      // orderDTO -> order 필드값 복사
        order.setOrderId(orderId);                        // 발주등록번호
        order.setStatus("발주등록(저장)");                  // 발주상태
        order.setManager(managerRepository.findById(orderDTO.getManager()).orElse(null));
        order.setSupplier(supplierRepository.findById(orderDTO.getSupplierCode()).orElse(null));
        orderRepository.save(order);

        // 발주 품목정보 저장
        for (OrderItemsDTO item : orderItems) {
            OrderItems orderItem = new OrderItems();
            BeanUtils.copyProperties(item, orderItem);
            orderItem.setItem(itemRepository.findById(item.getItemCode()).orElse(null));
            orderItem.setOrder(order);
            orderItem.setOrderItemId(orderId + item.getItemCode());
            log.info(orderItem.toString());
            orderItemsRepository.save(orderItem);
        }
    }

    public boolean checkValidation(OrderDTO orderDTO) {
        String managerId = orderDTO.getManager();
        String supplierCode = orderDTO.getSupplierCode();
        Optional<Manager> manager = managerRepository.findById(managerId);
        Optional<Supplier> supplier = supplierRepository.findById(supplierCode);
        // 입력한 매니저아이디, 거래처코드가 DB에 존재하는 값인지 검증
        return manager.isPresent() && supplier.isPresent();
    }

    public String generateNextOrderId() {
        String maxOrderId = orderRepository.findMaxOrderId();

        String newOrderId = null;

        if (maxOrderId == null) {
            newOrderId = "OD0001";
        } else {
            int numberPart = Integer.parseInt(maxOrderId.substring(2));
            newOrderId = String.format("OD%04d", numberPart + 1);
        }
        log.info("TXService: generateNextOrderId + newOrderId = " + newOrderId);

        return newOrderId;
    }

    public String getManagerName(String managerId) {
        Optional<Manager> manager = managerRepository.findById(managerId);
        if (manager.isPresent()) {
            return manager.get().getManagerId();
        } else {
            throw new NoSuchElementException(managerId + "에 해당하는 매니저 없음");
        }
    }

    public List<Manager> getAllManagers() {
        return managerRepository.findAll();
    }

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public List<Item> getOrderItems() {
        return itemRepository.findAll();
        // 사용중인 코드 + 구매할 수 있는 아이템 한정, 재고수량 오름차순으로 수정 필요
    }

    public List<OrderDTO> getOrderList() {

        log.info("TXService: getOrderList");
        List<Order> allOrders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "orderId"));

        return getOrderDTOS(allOrders);

    }

    public Order getOrderById(String orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isPresent()) {
            return order.get();
        } else {
            throw new NoSuchElementException(orderId + "에 해당하는 발주 없음");
        }
    }

    public List<OrderItems> getOrderedItems(Order order) {
        return orderItemsRepository.findByOrder(order);
    }

    public List<Item> getSaleItems() {
        return itemRepository.findAll();
        // 사용중인 코드 + 판매할 수 있는 아이템 한정, 재고수량 오름차순으로 수정 필요
    }

    public List<OrderDTO> searchOrders(String status, String supplierName, String orderDateStart, String orderDateEnd,
                                       String itemName, String dueDateStart, String dueDateEnd) {
        log.info("TXService: searchOrders");

        // 날짜 자료형 String -> Timestamp 변경
        Timestamp orderStart = convertToTimestamp(orderDateStart);
        Timestamp orderEnd = convertToTimestamp(orderDateEnd);
        Timestamp dueStart = convertToTimestamp(dueDateStart);
        Timestamp dueEnd = convertToTimestamp(dueDateEnd);

        String formattedStatus = (status != null && !status.trim().isEmpty()) ? status : null;

        // LIKE 검색할 것들 % 붙여주기
        String formattedSupplierName = supplierName != null && !supplierName.trim().isEmpty() ? "%" + supplierName + "%" : null;
        String formattedItemName = itemName != null && !itemName.trim().isEmpty() ? "%" + itemName + "%" : null;

        log.info("status: " + formattedStatus + " supplierName: " + formattedSupplierName + " orderDateStart: " + orderStart + " orderDateEnd: " + orderEnd + " itemName: " + formattedItemName + " dueStart: " + dueStart + " dueEnd: " + dueEnd);

        List<Order> ordersByConditions = orderRepository.findOrdersByConditions
                (formattedStatus, formattedSupplierName, orderStart, orderEnd, formattedItemName, dueStart, dueEnd);

        log.info("TXService: searchOrdersByConditions" + ordersByConditions);

        return getOrderDTOS(ordersByConditions);
    }

    private Timestamp convertToTimestamp(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        LocalDate dateTime = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return Timestamp.valueOf(dateTime.atStartOfDay());
    }

    private List<OrderDTO> getOrderDTOS(List<Order> ordersByConditions) {
        return ordersByConditions.stream()
                .map(order -> {
                    OrderDTO orderDTO = new OrderDTO();
                    orderDTO.setOrderId(order.getOrderId());
                    orderDTO.setTotalPrice(order.getTotalPrice());
                    orderDTO.setOrderDate(order.getOrderDate());
                    orderDTO.setDueDate(order.getDueDate());
                    orderDTO.setStatus(order.getStatus());
                    orderDTO.setSupplierName(orderRepository.findSupplierNameByOrderId(order.getOrderId()));
                    List<String> firstItem = orderRepository.findFirstItemNameByOrder(order);
                    orderDTO.setItemName(firstItem.isEmpty() ? null : firstItem.get(0));
                    orderDTO.setItemCount(orderRepository.findOrderItemCountByOrder(order));
                    log.info("TXService: getOrderDTOS: " + orderDTO);
                    return orderDTO;
                })
                .collect(Collectors.toList());
    }

    public void updateOrderStatus(String orderId, String status) {
        log.info("TXService: updateOrderStatus");
        orderRepository.updateOrderStatusById(status, orderId);
    }

}
