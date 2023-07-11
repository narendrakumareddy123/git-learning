package com.homework.supplychainmgmt.controller;

import com.homework.supplychainmgmt.dao.UserRepository;
import com.homework.supplychainmgmt.model.Order;
import com.homework.supplychainmgmt.model.User;
import com.homework.supplychainmgmt.service.OrderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;

@RestController
@AllArgsConstructor
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @PreAuthorize("hasRole(@roles.ADMIN)")
    @GetMapping(value = "/orders/{orderId}", produces = "application/json")
    public Order findOrderById(@PathVariable("orderId") int id) throws DataAccessException {
        Order order = orderService.findOrderById(id);
        if (order == null){
            return null;
        }

        return order;
    }

    @PreAuthorize("hasRole(@roles.ADMIN)")
    @GetMapping(value = "/orders", produces = "application/json")
    public Collection<Order> findAllOrders() throws DataAccessException {
        Collection<Order>orders = new ArrayList<>();
        orders.addAll(orderService.findAllOrders());
        if (orders.isEmpty()){
            return null;
        }
        return orders;
    }

    @PreAuthorize("hasRole(@roles.CLIENT)")
    @GetMapping(value = "/orders/myorders", produces = "application/json")
    public Collection<Order> findAllMyOrders(Authentication auth) throws DataAccessException {
        User user = userRepository.findUserByUsername(auth.getName());
        Collection<Order>orders = new ArrayList<>();
        orders.addAll(orderService.findAllMyOrders(user.getId()));
        if (orders.isEmpty()){
            return null;
        }
        return orders;
    }

    @PreAuthorize("hasRole(@roles.MANUFACTURER)")
    @GetMapping(value = "/orders/ordersforme", produces = "application/json")
    public Collection<Order> findAllOrdersForMe(Authentication auth) throws DataAccessException {
        User user = userRepository.findUserByUsername(auth.getName());
        Collection<Order>orders = new ArrayList<>();
        orders.addAll(orderService.findAllOrdersForMe(user.getId()));
        if (orders.isEmpty()){
            return null;
        }
        return orders;
    }

    @PreAuthorize("hasRole(@roles.CLIENT)")
    @DeleteMapping(value = "/orders/{orderId}", produces = "application/json")
    @Transactional
    public void cancelOrder(@PathVariable("orderId") int orderId, Authentication auth){
        User user = userRepository.findUserByUsername(auth.getName());
        Order order = orderService.findOrderById(orderId);
        log.info("Before cancel");
        log.info("Detalis: " + "Order clientId = "+ order.getClientId() + " username: " +auth.getName() + " userId: " + user.getId());
        if(order.getClientId() == user.getId()){
            orderService.cancelOrder(orderId);
            log.info("Order canceled!");
        }
    }

    @PreAuthorize("hasRole(@roles.MANUFACTURER)")
    @PatchMapping(value = "/orders/{orderId}", consumes = "application/json", produces = "application/json")
    public Order setOrderStatus(@PathVariable("orderId") int orderId , @RequestBody String status, Authentication auth){
        User user = userRepository.findUserByUsername(auth.getName());
        Order order = orderService.findOrderById(orderId);
        if(order.getManufacturerId() == user.getId()){
            orderService.setOrderStatus(orderId, status);
            log.info("Order`s status has been changed!");
        }
        return order;
    }

    @PreAuthorize("hasRole(@roles.CLIENT)")
    @PostMapping(value = "/orders", consumes = "application/json", produces = "application/json")
    public Order addOrder(@RequestBody @Valid Order order){
        return orderService.addOrder(order);
    }
}
