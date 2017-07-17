package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by Summer on 2017/6/29.
 */
public class Conts {

    public static final String CURRENT_USER = "current_User";

    public static final String EMAIL ="email";
    public static final String USERNAME ="username";



    //根据这个判断是升序还是降序
    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }


    //规定用户和管理员的角色（根据这个判断状态）
    public interface Role{
        int ROLE_CUSTOM=0;//普通用户
        int ROLE_ADMIN=1;//管理员
    }

    //购物车是否选中状态
    public interface CartCheck{
        int CHECKED = 1;//购物车选中状态
        int UN_CHECKED = 0;//购物车未选中状态

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }




    //产品在架或下架状态
    public enum ProsuctSaleStatus{
        ON_SALE(1,"在线");
        private int code;
        private String value;

        ProsuctSaleStatus(int code,String value){
            this.code = code;
            this.value = value;
        }


        public int getCode() {
            return code;
        }


        public String getValue() {
            return value;
        }

    }


    /**
     * 关于订单状态的枚举
     */
    public enum OrderStatusEnum{
        CANCELED(0,"已取消"),
        NO_PAY(10,"未支付"),
        PAID(20,"已付款"),
        SHIPPED(40,"已发货"),
        ORDER_SUCCESS(50,"订单完成"),
        ORDER_CLOSE(60,"订单关闭");

        private int code;
        private String values;

        OrderStatusEnum(int code,String values){
            this.code = code;
            this.values = values;
        }

        public int getCode() {
            return code;
        }

        public String getValues() {
            return values;
        }


        public static OrderStatusEnum codeof(int code){
            for (OrderStatusEnum orderStatusEnum : values()){
                if (orderStatusEnum.getCode() == code){
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举！！！");
        }

    }


    /**
     * 支付宝状态接口
     */
    public interface alipayCallback{

        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";


        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED="failed";
    }



    //选择支付平台
    public enum PayPlatFormEnum{
        ALIPAY(1,"支付宝");
        //在这里可以加微信支付
        //WECHATPAY(2,"微信支付")

        private int code;
        private String values;

        PayPlatFormEnum(int code,String values){
            this.code=code;
            this.values=values;
        }

        public int getCode() {
            return code;
        }

        public String getValues() {
            return values;
        }
    }



    //付款方式：在线支付或者货到付款
    public enum PaymentTypeEnum{

        ONLINE_PAY(1,"在线支付"),
        CASH_PAY(2,"货到付款");

        private int code;
        private String values;


        PaymentTypeEnum(int code,String values){
            this.code=code;
            this.values=values;
        }


        public int getCode() {
            return code;
        }

        public String getValues() {
            return values;
        }



        public static PaymentTypeEnum codeof(int code){
            for (PaymentTypeEnum paymentTypeEnum : values()){
                if (paymentTypeEnum.getCode() == code){
                    return paymentTypeEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举！！！");
        }
    }







}
