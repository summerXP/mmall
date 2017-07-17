package com.mmall.controller.protal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Conts;
import com.mmall.common.ResponseCode;
import com.mmall.common.SeverResponse;
import com.mmall.pojo.Shipping;
import com.mmall.pojo.User;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Created by Summer on 2017/7/10.
 * Desc:收货地址模块控制器
 */
@RequestMapping("/shipping/")
@Controller
public class ShippingController {


    @Autowired
    private IShippingService iShippingService;


    /**
     *添加收货地址
     * @param session
     * @param shipping
     * @return
     */
    @RequestMapping("add.do")
    @ResponseBody
    public SeverResponse add(HttpSession session, Shipping shipping){
        User user = (User) session.getAttribute(Conts.CURRENT_USER);
        if (user == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return iShippingService.add(user.getId(),shipping);
    }


    /**
     * 删除收货地址
     * @param session
     * @param shippingId
     * @return
     */
    @RequestMapping("delete.do")
    @ResponseBody
    public SeverResponse delete(HttpSession session,Integer shippingId){
        User user = (User) session.getAttribute(Conts.CURRENT_USER);
        if (user == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return  iShippingService.delete(user.getId(),shippingId);
    }


    /**
     * 修改收获地址
     * @param session
     * @param shipping
     * @return
     */
    @RequestMapping("update.do")
    @ResponseBody
    public SeverResponse update(HttpSession session,Shipping shipping){
        User user = (User) session.getAttribute(Conts.CURRENT_USER);
        if (user == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.update(user.getId(),shipping);
    }


    /**
     * 查询收货地址
     * @param session
     * @param shippingId
     * @return
     */
    @RequestMapping("select.do")
    @ResponseBody
    public SeverResponse<Shipping> select(HttpSession session,Integer shippingId){

        User user = (User) session.getAttribute(Conts.CURRENT_USER);
        if (user == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.select(user.getId(),shippingId);
    }


    /**
     * 查询到当前用户下面的所有收货地址
     * @param pageNum
     * @param pageSize
     * @param session
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public SeverResponse<PageInfo> list(@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                        @RequestParam(value = "pageSize",defaultValue = "10") int pageSize,
                                        HttpSession session){

        User user = (User) session.getAttribute(Conts.CURRENT_USER);
        if (user == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.list(user.getId(),pageNum,pageSize);
    }
}
