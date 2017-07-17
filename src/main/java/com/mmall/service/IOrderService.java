package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.SeverResponse;
import com.mmall.vo.OrderVo;

import java.util.Map;

/**
 * Created by Summer on 2017/7/11.
 * Desc:订单模块接口
 */
public interface IOrderService {

    SeverResponse pay(Integer userId, Long orderNum, String path);

    SeverResponse aliCallback(Map<String,String> params);

    SeverResponse queryOrderPayStatus(Integer userId,Long orderNo);

    SeverResponse createOrder(Integer userId,Integer shippingId);

    SeverResponse cancel(Integer userId,Long orderNo);

    SeverResponse getOrderCartProduct(Integer userId);

    SeverResponse getOrderDetail(Integer userId,Long orderNo);

    SeverResponse<PageInfo> getList(Integer userId, int pageNum, int pageSize);


    //backend
    SeverResponse<PageInfo> manageList(int pageNum,int pageSize);
    SeverResponse<OrderVo> manageDetail(Long orderNo);
    SeverResponse<PageInfo> manageSearch(Long orderNo,int pageNum,int pageSize);
    SeverResponse manageSendGoods(Long orderNo);
}
