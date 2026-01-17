package com.architecture.application.service;

import com.architecture.application.dto.AddOrderItemRequest;
import com.architecture.application.dto.CreateOrderRequest;
import com.architecture.application.dto.OrderDTO;
import com.architecture.application.dto.OrderItemDTO;
import com.architecture.domain.aggregate.Order;
import com.architecture.domain.aggregate.OrderItem;
import com.architecture.domain.entity.Customer;
import com.architecture.domain.entity.Product;
import com.architecture.domain.valueobject.Address;
import com.architecture.infrastructure.repository.CustomerRepository;
import com.architecture.infrastructure.repository.OrderRepository;
import com.architecture.infrastructure.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderApplicationService {
    
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderApplicationService(OrderRepository orderRepository, 
                                   CustomerRepository customerRepository,
                                   ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
            .orElseThrow(() -> new IllegalArgumentException("客户不存在"));

        Address shippingAddress = new Address(
            request.getProvince(),
            request.getCity(),
            request.getDistrict(),
            request.getStreet(),
            request.getZipCode()
        );

        Long orderId = orderRepository.nextId();
        Order order = new Order(orderId, customer, shippingAddress);
        
        orderRepository.save(order);

        return convertToDTO(order);
    }

    @Transactional
    public OrderDTO addOrderItem(AddOrderItemRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new IllegalArgumentException("商品不存在"));

        order.addOrderItem(product, request.getQuantity());
        
        orderRepository.save(order);
        productRepository.save(product);

        return convertToDTO(order);
    }

    @Transactional
    public void confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        order.confirm();
        orderRepository.save(order);
    }

    @Transactional
    public void shipOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        order.ship();
        orderRepository.save(order);
    }

    @Transactional
    public void deliverOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        order.deliver();
        orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        order.cancel();
        
        for (OrderItem item : order.getOrderItems()) {
            productRepository.save(item.getProduct());
        }
        
        orderRepository.save(order);
    }

    public OrderDTO getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
        return convertToDTO(order);
    }

    public List<OrderDTO> getCustomerOrders(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        return orders.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setCustomerId(order.getCustomer().getId());
        dto.setCustomerName(order.getCustomer().getName());
        dto.setStatus(order.getStatus().getDescription());
        dto.setShippingAddress(order.getShippingAddress().getFullAddress());
        dto.setOrderTime(order.getOrderTime());
        dto.setDeliveryTime(order.getDeliveryTime());
        dto.setTotalAmount(order.getTotalAmount().getAmount());
        dto.setCurrency(order.getTotalAmount().getCurrency());

        List<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
            .map(this::convertItemToDTO)
            .collect(Collectors.toList());
        dto.setItems(itemDTOs);

        return dto;
    }

    private OrderItemDTO convertItemToDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice().getAmount());
        dto.setSubTotal(item.getSubTotal().getAmount());
        dto.setCurrency(item.getUnitPrice().getCurrency());
        return dto;
    }
}
