package com.mmall.service;

import com.mmall.common.SeverResponse;
import com.mmall.vo.CartVo;

/**
 * Created by Summer on 2017/7/7.
 * Desc:购物车的接口
 */
public interface ICartServicr {

    SeverResponse<CartVo> add(Integer userId, Integer count, Integer productId);

    SeverResponse<CartVo> update(Integer userId,Integer productId,Integer count);

    SeverResponse<CartVo> deleteProduct(Integer userId,String productIds);

    SeverResponse<CartVo> selectOrUnSelect (Integer userId,Integer productId,Integer checked);

    SeverResponse<Integer> getCartProductCount(Integer userId);

    SeverResponse<CartVo> list(Integer userId);
}
