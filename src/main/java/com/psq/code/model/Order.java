package com.psq.code.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description 订单实体类
 * @Author psq
 * @Date 2021/11/24 16:46
 */
public class Order implements Serializable {
    private Long orderId;
    private String type;
    private String result;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Order() {
    }

    public Order(Long orderId, String type, String result) {
        this.orderId = orderId;
        this.type = type;
        this.result = result;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", type='" + type + '\'' +
                '}';
    }

    /**
     * 做的假数据
     * @return
     */
    public static List<Order> getOrderList(){
        List<Order> list = new ArrayList<>();
        list.add(new Order(11L, "创建订单",""));
        list.add(new Order(22L, "创建订单",""));
        list.add(new Order(33L, "创建订单",""));
        list.add(new Order(11L, "支付订单",""));
        list.add(new Order(22L, "支付订单",""));
        list.add(new Order(33L, "支付订单",""));
        list.add(new Order(11L, "完成订单",""));
        list.add(new Order(22L, "完成订单",""));
        list.add(new Order(33L, "完成订单",""));
        list.add(new Order(11L, "物流订单",""));
        list.add(new Order(22L, "物流订单",""));
        list.add(new Order(33L, "物流订单",""));
        return list;
    }
}
