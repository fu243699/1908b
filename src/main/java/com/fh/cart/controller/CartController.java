package com.fh.cart.controller;

import com.alibaba.fastjson.JSONObject;
import com.fh.cart.model.Cart;
import com.fh.cart.service.CartService;
import com.fh.common.MemberAnnotation;
import com.fh.common.ServerResponse;
import com.fh.member.model.Member;
import com.fh.redis.RedisUtil;
import com.fh.util.SystemConstant;
import com.sun.xml.internal.ws.developer.MemberSubmissionAddressing;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("cart")
public class CartController {
    @Resource
    private CartService cartService;
    @RequestMapping("buy")
    public ServerResponse buy(Cart cart, HttpServletRequest request){
        return cartService.buy(cart,request);
    }
    @RequestMapping("queryCartProductCount")
    public ServerResponse queryCartProductCount(HttpServletRequest request){
        Member member = (Member) request.getSession().getAttribute(SystemConstant.SESSION_KEY);
        List<String> stringList = RedisUtil.hGet(SystemConstant.REDIS_CART_KEY + member.getId());
        long totalCount = 0;
        if(stringList != null && stringList.size() > 0) {
            for (String str : stringList) {
                Cart cart = JSONObject.parseObject(str, Cart.class);
                totalCount += cart.getCount();
            }
        }else {
            return ServerResponse.success(0);
        }
        return ServerResponse.success(totalCount);
    }

    @RequestMapping("queryCartListCount")
    public ServerResponse queryCartListCount(@MemberAnnotation Member member){
        return cartService.queryCartListCount(member);
    }

    @RequestMapping("queryCartList")
    public ServerResponse queryCartList(@MemberAnnotation Member member){
        return cartService.queryCartList(member);
    }
    @RequestMapping("del/{productId}")
    public ServerResponse del(@MemberAnnotation Member member, @PathVariable("productId") Integer productId){
        RedisUtil.del(SystemConstant.REDIS_CART_KEY +member.getId(),productId.toString());
        return cartService.queryCartList(member);
    }

    @RequestMapping("batchDel")
    public ServerResponse batchDel(@MemberAnnotation Member member, @RequestParam("idList") List<Integer> idList){
        for (Integer productId : idList) {
            RedisUtil.del(SystemConstant.REDIS_CART_KEY +member.getId(),productId.toString());
        }
        return cartService.queryCartList(member);
    }



}
