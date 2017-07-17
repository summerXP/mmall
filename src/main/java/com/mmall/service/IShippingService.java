package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.SeverResponse;
import com.mmall.pojo.Shipping;

/**
 * Created by Summer on 2017/7/10.
 * Desc:收货地址是接口
 */
public interface IShippingService {

    SeverResponse add(Integer userId, Shipping shipping);

    SeverResponse<String> delete(Integer userId,Integer shippingId);

    SeverResponse update(Integer userId,Shipping shipping);

    SeverResponse<Shipping> select(Integer userId,Integer shippingId);

    SeverResponse<PageInfo> list(Integer userId, int pageNum, int pageSize);
}
