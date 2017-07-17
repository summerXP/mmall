package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.SeverResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Summer on 2017/7/10.
 * Desc:收获地址的接口实现类
 */
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;


    /**
     * 添加收货地址
     * @param userId
     * @param shipping
     * @return
     */
    public SeverResponse add(Integer userId,Shipping shipping){

        shipping.setUserId(userId);
        int rawCount = shippingMapper.insert(shipping);
        if (rawCount > 0){
            Map result = Maps.newHashMap();
            result.put("shippingId",shipping.getId());
            return SeverResponse.createBySuccess("新建收货地址成功！！！",result);
        }
        return SeverResponse.createByErrorMessage("新建收货地址失败！！！");
    }


    /**
     * 删除收货地址
     * @param userId
     * @param shippingId
     * @return
     */
    public SeverResponse<String> delete(Integer userId,Integer shippingId){
        int rawCount = shippingMapper.deleteByShippingIdUserId(userId,shippingId);
        if (rawCount > 0){
            return SeverResponse.createBySuccess("删除地址成功！！！");
        }
        return SeverResponse.createByErrorMessage("删除地址失败！！！");
    }


    /**
     * 修改收货地址
     * @param userId
     * @param shipping
     * @return
     */
    public SeverResponse update(Integer userId,Shipping shipping){

        shipping.setUserId(userId);
        int rawCount = shippingMapper.updateByShipping(shipping);
        if (rawCount > 0){
            return SeverResponse.createBySuccess("修改收货地址成功！！！");
        }

        return SeverResponse.createByErrorMessage("修改收货地址失败！！！");
    }


    /**
     * 查询单个地址
     * @param userId
     * @param shippingId
     * @return
     */
    public SeverResponse<Shipping> select(Integer userId,Integer shippingId){

        Shipping shipping = shippingMapper.selectbyShippingIdUserId(userId, shippingId);
        if (shipping == null){
            return SeverResponse.createByErrorMessage("无法查询到该地址！！！");
        }

        return SeverResponse.createBySuccess("查询地址成功！！！",shipping);
    }


    /**
     * 获取当前用户的所有收货地址
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public SeverResponse<PageInfo> list(Integer userId,int pageNum,int pageSize){

        PageHelper.startPage(pageNum, pageSize);

        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);

        PageInfo pageInfo = new PageInfo(shippingList);

        return SeverResponse.createBySuccess(pageInfo);
    }















}
