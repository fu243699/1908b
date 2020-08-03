package com.fh.order.service.orderService;


import com.fh.common.ServerResponse;
import com.fh.member.model.Member;

public interface OrderService {


    ServerResponse buildOrderList(Member member, String listJson, Integer addressId);

    ServerResponse queryOrderList();

    ServerResponse changeOrderStatus(String orderNo);
}
