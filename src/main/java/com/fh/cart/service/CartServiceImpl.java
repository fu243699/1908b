package com.fh.cart.service;

import com.alibaba.fastjson.JSONObject;
import com.fh.cart.model.Cart;
import com.fh.common.ServerEnum;
import com.fh.common.ServerResponse;
import com.fh.member.model.Member;
import com.fh.product.model.Product;
import com.fh.product.service.ProductService;
import com.fh.redis.RedisUtil;
import com.fh.util.SystemConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private ProductService productService;

    @Override
    public ServerResponse buy(Cart cart, HttpServletRequest request) {
        //查询商品是否存在
        Product product =productService.isExist(cart.getProductId());
        if(product == null){
            return ServerResponse.error(ServerEnum.EXIST_ERROR);
        }
        //查询商品是否上架
        if(product.getStatus() == 0){
            return ServerResponse.error(ServerEnum.STATUS_ERROR);
        }
        //查询商品是否在redis count
        Member member = (Member) request.getSession().getAttribute(SystemConstant.SESSION_KEY);
        Boolean exists = RedisUtil.hExists(SystemConstant.REDIS_CART_KEY + member.getId(), cart.getProductId().toString());
        if(exists){
            //购物车存在数据
            String cartJson = RedisUtil.hGet(SystemConstant.REDIS_CART_KEY + member.getId(), cart.getProductId().toString());
            Cart cartRedis = JSONObject.parseObject(cartJson, Cart.class);
            cartRedis.setCount(cartRedis.getCount()+cart.getCount());
            String toJSONString = JSONObject.toJSONString(cartRedis);
            RedisUtil.hSet(SystemConstant.REDIS_CART_KEY + member.getId(), cart.getProductId().toString(),toJSONString);
        }else{
            //完善cart信息
            cart.setFilePath(product.getFilePath());
            cart.setPrice(product.getPrice());
            cart.setName(product.getName());
            String toJSONString = JSONObject.toJSONString(cart);
            RedisUtil.hSet(SystemConstant.REDIS_CART_KEY + member.getId(), cart.getProductId().toString(),toJSONString);
        }
        return ServerResponse.success();
    }

    @Override
    public ServerResponse queryCartListCount(Member member) {
        //查询商品是否在redis count
        int sum = 0;
        if(member != null){
            List<String> hVals = RedisUtil.hGet(SystemConstant.REDIS_CART_KEY + member.getId());
            List<Cart> cartList = new ArrayList<>();
            if(hVals != null && hVals.size() > 0){
                for (String jsonString : hVals) {
                    Cart cartRedis = JSONObject.parseObject(jsonString, Cart.class);
                    sum +=cartRedis.getCount();
                    cartList.add(cartRedis);
                }
            }
        }
        return ServerResponse.success(sum);
    }

    @Override
    public ServerResponse queryCartList(Member member) {
        int sum = 0;
        List<Cart> cartList = new ArrayList<>();
        if(member != null){
            List<String> hVals = RedisUtil.hGet(SystemConstant.REDIS_CART_KEY + member.getId());
            if(hVals != null && hVals.size() > 0){
                for (String jsonString : hVals) {
                    Cart cartRedis = JSONObject.parseObject(jsonString, Cart.class);
                    sum +=cartRedis.getCount();
                    cartList.add(cartRedis);
                }
            }
        }
        Map<String,Object> map = new HashMap<>();
        map.put("cartList",cartList);
        map.put("sum",sum);
        return ServerResponse.success(map);
    }
}
