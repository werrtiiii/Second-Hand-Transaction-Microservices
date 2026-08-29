package com.secondhand.order.service;

import com.secondhand.common.AppException;
import com.secondhand.order.entity.*;
import com.secondhand.order.repository.*;
import com.secondhand.micro.platform.ProductSnapshot;


import com.secondhand.micro.platform.ProductClient;
import com.secondhand.rating.entity.Rating;
import com.secondhand.rating.repository.RatingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final OrderEventRepository orderEventRepo;
    private final ShipmentRepository shipmentRepo;
    private final ProductClient productService;
    @org.springframework.beans.factory.annotation.Autowired private com.secondhand.micro.trade.TradeStore saga;
    @org.springframework.beans.factory.annotation.Autowired private com.secondhand.micro.trade.PaymentLedger ledger;
    @org.springframework.beans.factory.annotation.Autowired private com.secondhand.micro.platform.Outbox outbox;
    private final RatingRepository ratingRepo;

    public OrderService(OrderRepository orderRepo, OrderEventRepository orderEventRepo,
                        ShipmentRepository shipmentRepo, ProductClient productService,
                        RatingRepository ratingRepo) {
        this.orderRepo = orderRepo;
        this.orderEventRepo = orderEventRepo;
        this.shipmentRepo = shipmentRepo;
        this.productService = productService;

        this.ratingRepo = ratingRepo;
    }

    public Order cancel(long buyerId,long orderId){saga.cancel(buyerId,orderId);return orderRepo.findById(orderId).orElseThrow();}

    public record CreateOrderCommand(long productId, String receiverName, String receiverPhone, String receiverAddress, Long addressId) {}
    public record ShipCommand(String carrierCode, String trackingNo) {}
    public record OrderDetail(Order order, Shipment shipment, List<OrderEvent> events,
                              boolean canPay, boolean canShip, boolean canConfirm,
                              boolean canCancel, boolean canApplyAfterSale,
                              boolean fundsInEscrow, LocalDateTime settlementDueAt) {}

    public record SoldProductDto(Long orderId, Long productId, String productTitle,
                                 String productCover, Integer priceCent,
                                 LocalDateTime completedAt, Integer ratingScore,
                                 String ratingComment) {}

    /** 卖家已售出的订单（已完成），含评分 */
    @Transactional(readOnly = true)
    public List<SoldProductDto> getSellerSoldProducts(Long sellerId) {
        List<Order> completed = orderRepo.findBySellerIdAndStatusOrderByCreatedAtDesc(
                sellerId, OrderStatus.COMPLETED, org.springframework.data.domain.Pageable.unpaged())
                .getContent();
        List<SoldProductDto> result = new ArrayList<>();
        for (Order o : completed) {
            ProductSnapshot p;
            try { p = productService.getById(o.getProductId()); }
            catch (Exception e) { continue; }

            Rating rating = ratingRepo.findByOrderId(o.getId()).orElse(null);

            result.add(new SoldProductDto(
                    o.getId(), o.getProductId(), p.getTitle(), p.getCoverImageUrl(),
                    o.getAmountCent(), o.getCompletedAt(),
                    rating != null ? rating.getScore() : null,
                    rating != null ? rating.getComment() : null
            ));
        }
        return result;
    }


    @Transactional
    public Order pay(long buyerId, long orderId) {
        Order order = orderRepo.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> new AppException("NOT_FOUND", "订单不存在", HttpStatus.NOT_FOUND));
        if (order.getStatus() != OrderStatus.WAIT_PAY) {
            throw new AppException("CONFLICT", "订单状态不允许支付", HttpStatus.CONFLICT);
        }
        if (order.getReceiverName() == null || order.getReceiverName().isBlank()
                || order.getReceiverPhone() == null || order.getReceiverPhone().isBlank()
                || order.getReceiverAddress() == null || order.getReceiverAddress().isBlank()) {
            throw new AppException("BAD_REQUEST", "请先填写收货信息再支付", HttpStatus.BAD_REQUEST);
        }
        ledger.pay(order.getId(),buyerId);
        OrderStatus from = order.getStatus();
        order.setStatus(OrderStatus.WAIT_DELIVER);
        order.setPaidAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        Order saved = orderRepo.save(order);
        appendEvent(saved.getId(), from, OrderStatus.WAIT_DELIVER, "买家已支付");
        return saved;
    }

    /** 兼容旧 pay 方法名 */
    @Transactional
    public Order markPaid(long buyerId, long orderId) {
        return pay(buyerId, orderId);
    }

    @Transactional
    public Shipment ship(long sellerId, long orderId, ShipCommand cmd) {
        Order order = orderRepo.findByIdAndSellerId(orderId, sellerId)
                .orElseThrow(() -> new AppException("NOT_FOUND", "订单不存在", HttpStatus.NOT_FOUND));
        if (order.getStatus() != OrderStatus.WAIT_DELIVER) {
            throw new AppException("CONFLICT", "当前状态不允许发货", HttpStatus.CONFLICT);
        }

        LocalDateTime now = LocalDateTime.now();
        Shipment shipment = shipmentRepo.findByOrderId(order.getId()).orElse(null);
        if (shipment == null) {
            shipment = new Shipment();
            shipment.setOrderId(order.getId());
            shipment.setCreatedAt(now);
        }
        shipment.setCarrierCode(cmd.carrierCode());
        shipment.setTrackingNo(cmd.trackingNo());
        shipment.setStatus(ShipmentStatus.CREATED);
        shipment.setUpdatedAt(now);
        Shipment savedShipment = shipmentRepo.save(shipment);

        OrderStatus from = order.getStatus();
        order.setStatus(OrderStatus.WAIT_RECEIVE);
        order.setShippedAt(now);
        order.setUpdatedAt(now);
        orderRepo.save(order);
        appendEvent(order.getId(), from, OrderStatus.WAIT_RECEIVE, "卖家已发货 (" + cmd.carrierCode() + " " + cmd.trackingNo() + ")");
        return savedShipment;
    }

    @Transactional
    public Order confirmReceived(long buyerId, long orderId) {
        Order order = orderRepo.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> new AppException("NOT_FOUND", "订单不存在", HttpStatus.NOT_FOUND));
        if (order.getStatus() != OrderStatus.WAIT_RECEIVE) {
            throw new AppException("CONFLICT", "当前状态不允许确认收货", HttpStatus.CONFLICT);
        }
        OrderStatus from = order.getStatus();
        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        Order saved = orderRepo.save(order);
        appendEvent(saved.getId(), from, OrderStatus.COMPLETED,
                "买家已确认收货，资金由平台托管中（7天售后期满后自动结算给卖家）");
        return saved;
    }

    /** 结算资金给卖家（售后期满自动调用） */
    @Transactional
    public Order settleFunds(long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new AppException("NOT_FOUND", "订单不存在", HttpStatus.NOT_FOUND));
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new AppException("CONFLICT", "当前状态不允许结算", HttpStatus.CONFLICT);
        }
        LocalDateTime now = LocalDateTime.now();
        order.setStatus(OrderStatus.SETTLED);
        order.setSettledAt(now);
        order.setUpdatedAt(now);
        Order saved = orderRepo.save(order);
        appendEvent(saved.getId(), OrderStatus.COMPLETED, OrderStatus.SETTLED,
                "售后期满，资金已结算给卖家，订单彻底完结");
        return saved;
    }

    /** 定时任务：自动结算超过7天售后期的订单 */
    @Transactional
    public int processSettlements() {
        List<Order> toSettle = orderRepo.findByStatusAndCompletedAtBefore(
                OrderStatus.COMPLETED, LocalDateTime.now().minusDays(7));
        int count = 0;
        for (Order order : toSettle) {
            settleFunds(order.getId());
            count++;
        }
        return count;
    }



    @Transactional
    public Order updateReceiver(long userId, long orderId, String receiverName,
                                 String receiverPhone, String receiverAddress) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new AppException("NOT_FOUND", "订单不存在", HttpStatus.NOT_FOUND));
        if (!order.getBuyerId().equals(userId)) {
            throw new AppException("FORBIDDEN", "只有买家可以修改收货信息", HttpStatus.FORBIDDEN);
        }
        if(order.getStatus()!=OrderStatus.WAIT_PAY)throw new AppException("CONFLICT","只能修改待支付订单的地址",HttpStatus.CONFLICT);
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setReceiverAddress(receiverAddress);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepo.save(order);
    }

    @Transactional(readOnly = true)
    public OrderDetail getOrderDetail(long userId, long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new AppException("NOT_FOUND", "订单不存在", HttpStatus.NOT_FOUND));
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new AppException("FORBIDDEN", "无权查看该订单", HttpStatus.FORBIDDEN);
        }
        Shipment shipment = shipmentRepo.findByOrderId(orderId).orElse(null);
        List<OrderEvent> events = orderEventRepo.findByOrderIdOrderByIdAsc(orderId);

        OrderStatus s = order.getStatus();
        boolean isBuyer = order.getBuyerId().equals(userId);
        boolean isSeller = order.getSellerId().equals(userId);

        // 售后申请：仅确认收货后7天内可手动发起（收货前由超时自动触发）
        boolean canApplyAfterSale = isBuyer && s == OrderStatus.COMPLETED
                && order.getCompletedAt() != null
                && order.getCompletedAt().plusDays(7).isAfter(LocalDateTime.now());

        // 资金托管状态
        boolean fundsInEscrow = s == OrderStatus.WAIT_DELIVER
                || s == OrderStatus.WAIT_RECEIVE
                || s == OrderStatus.COMPLETED;
        LocalDateTime settlementDueAt = null;
        if (s == OrderStatus.COMPLETED && order.getCompletedAt() != null) {
            settlementDueAt = order.getCompletedAt().plusDays(7);
        }

        return new OrderDetail(order, shipment, events,
                isBuyer && s == OrderStatus.WAIT_PAY,                                      // canPay
                isSeller && s == OrderStatus.WAIT_DELIVER,                                 // canShip
                isBuyer && s == OrderStatus.WAIT_RECEIVE,                                  // canConfirm
                isBuyer && s == OrderStatus.WAIT_PAY,                                      // canCancel
                canApplyAfterSale,
                fundsInEscrow,
                settlementDueAt
        );
    }

    private void appendEvent(Long orderId, OrderStatus from, OrderStatus to, String note) {
        OrderEvent ev = new OrderEvent();
        ev.setOrderId(orderId);
        ev.setFromStatus(from == null ? "NONE" : from.name());
        ev.setToStatus(to.name());
        ev.setNote(note);
        ev.setCreatedAt(LocalDateTime.now());
        orderEventRepo.save(ev);
        Order order=orderRepo.findById(orderId).orElseThrow();
        var message=java.util.Map.of("id","order-"+ev.getId(),"type","order_event","title","订单 #"+orderId,"content",note,"relatedId",String.valueOf(orderId),"time",ev.getCreatedAt().toString());
        outbox.enqueue(order.getBuyerId(),"system",message);outbox.enqueue(order.getSellerId(),"system",message);
    }
}
