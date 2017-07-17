package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Conts;
import com.mmall.common.ResponseCode;
import com.mmall.common.SeverResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import com.mmall.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import javax.xml.ws.RequestWrapper;

/**
 * Created by Summer on 2017/7/17.
 */
@Controller
@RequestMapping("/manage/order")
public class OrderManagerController {

    @Autowired
    private IUserService iUserService;


    @Autowired
    private IOrderService iOrderService;


    /**
     * 获取订单的list
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public SeverResponse<PageInfo> orderList(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                             @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){

        User user = (User)session.getAttribute(Conts.CURRENT_USER);
        if(user == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");
        }

        //检验当前用户是否是管理员
        SeverResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            //成功，说明就是管理员
            //Todo 在这里写获取订单List的代码
            return iOrderService.manageList(pageNum, pageSize);

        }else{
            return SeverResponse.createByErrorMessage("当前用户不是管理员，无权限操作！！！");
        }
    }


    /**
     * 获取订单详情
     * @param orderNo
     * @return
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public SeverResponse<OrderVo> orderDetail(HttpSession session,Long orderNo){

        User user = (User)session.getAttribute(Conts.CURRENT_USER);
        if(user == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");
        }
        //检验当前用户是否是管理员
        SeverResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            //成功，说明就是管理员
            //Todo 在这里写获取订单详情的代码
            return iOrderService.manageDetail(orderNo);
        }else{
            return SeverResponse.createByErrorMessage("当前用户不是管理员，无权限操作！！！");
        }
    }


    /**
     * 搜索订单
     * @param session
     * @param orderNo
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("search.do")
    @ResponseBody
    public SeverResponse<PageInfo> orderSearch(HttpSession session,Long orderNo,@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                               @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){
        User user = (User)session.getAttribute(Conts.CURRENT_USER);
        if(user == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");
        }
        //检验当前用户是否是管理员
        SeverResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            //成功，说明就是管理员
            //Todo 在这里写搜索订单的代码
            return iOrderService.manageSearch(orderNo, pageNum, pageSize);
        }else{
            return SeverResponse.createByErrorMessage("当前用户不是管理员，无权限操作！！！");
        }
    }


    /**
     * 发货
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping("order_send_goods.do")
    @ResponseBody
    public SeverResponse<String> orderSendGoods(HttpSession session,Long orderNo){

        User user = (User)session.getAttribute(Conts.CURRENT_USER);
        if(user == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");
        }
        //检验当前用户是否是管理员
        SeverResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            //成功，说明就是管理员
            //Todo 在这里写管理员发货的代码
            return iOrderService.manageSendGoods(orderNo);
        }else{
            return SeverResponse.createByErrorMessage("当前用户不是管理员，无权限操作！！！");
        }
    }


}
