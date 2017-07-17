package com.mmall.controller.protal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mmall.common.Conts;
import com.mmall.common.ResponseCode;
import com.mmall.common.SeverResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Summer on 2017/7/11.
 */
@Controller
@RequestMapping("/order/")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);


    @Autowired
    private IOrderService iOrderService;


    /**
     * 创建订单
     * @param session
     * @param shippingId
     * @return
     */
    @RequestMapping("create.do")
    @ResponseBody
    public SeverResponse create(HttpSession session,Integer shippingId){
        User user = (User) session.getAttribute(Conts.CURRENT_USER);
        if (user == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return iOrderService.createOrder(user.getId(),shippingId);
    }


    /**
     * 取消订单（只能在未付款的情况下取消）
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping("cancel.do")
    @ResponseBody
    public SeverResponse cancel(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Conts.CURRENT_USER);
        if (user == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.cancel(user.getId(),orderNo);
    }


    /**
     * 比如说：购物车有10件，用户选中5件，剩余5件还在购物车中
     * 这个方法是选择用户选中的商品
     * @param session
     * @return
     */
    @RequestMapping("get_order_cart_product.do")
    @ResponseBody
    public SeverResponse getOrderCartProduct(HttpSession session){
        User user = (User) session.getAttribute(Conts.CURRENT_USER);
        if (user == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        return iOrderService.getOrderCartProduct(user.getId());
    }


    /**
     * 获取订单详情
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping("getdetail.do")
    @ResponseBody
    public SeverResponse detail(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Conts.CURRENT_USER);
        if (user == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderDetail(user.getId(),orderNo);
    }


    /**
     * 获取订单的List
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public SeverResponse list(HttpSession session,@RequestParam(value = "pageNum",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user = (User) session.getAttribute(Conts.CURRENT_USER);
        if (user == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getList(user.getId(),pageNum,pageSize);
    }









    /**
     * 支付订单
     * @param session
     * @param orderNo
     * @param request
     * @return
     */
    @RequestMapping("pay.do")
    @ResponseBody
    public SeverResponse pay(HttpSession session, Long orderNo, HttpServletRequest request){
        User user = (User)session.getAttribute(Conts.CURRENT_USER);
        if(user ==null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        String path = request.getSession().getServletContext().getRealPath("upload");

        return iOrderService.pay(user.getId(),orderNo,path);
    }




    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallBack(HttpServletRequest request){

        Map<String,String> params = Maps.newHashMap();

        Map requestParams = request.getParameterMap();
        //迭代器取出key值
        for (Iterator iterator = requestParams.keySet().iterator();iterator.hasNext();){
            String name = (String) iterator.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for(int i = 0 ; i < values.length; i++){
                valueStr = (i == values.length-1)?valueStr + values[i] : valueStr + values[i]+",";
            }

            params.put(name,valueStr);
        }

        logger.info("支付宝回调,sign:{},trade_status:{},参数:{}",params.get("sign"),params.get("trade_status"),params.toString());

        //非常重要,验证回调的正确性,是不是支付宝发的.并且呢还要避免重复通知.
        params.remove("sign_type");
        try {
            //支付宝验证
            boolean alipayRSACheckedV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());
            //如果为true，则验证正确，
            if (!alipayRSACheckedV2){
                return SeverResponse.createByErrorMessage("非法请求,验证不通过,再恶意请求我就报警了！！！");
            }

        } catch (AlipayApiException e) {
            logger.error("支付宝验证回调异常",e);
        }

        // TODO: 2017/7/12 在下面验证各种数据


        //
        SeverResponse severResponse = iOrderService.aliCallback(params);
        if (severResponse.isSuccess()){
            //System.out.println("回调true");
            return Conts.alipayCallback.RESPONSE_SUCCESS;
        }
        //System.out.println("回调false");
        return Conts.alipayCallback.RESPONSE_FAILED;
    }


    /**
     * 付款的时候，前台调用这个接口轮番查询，看是否付款成功了，如果付款成功，就跳转到完成界面
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public SeverResponse<Boolean> queryOrderPayStatus(HttpSession session, Long orderNo){
        User user = (User)session.getAttribute(Conts.CURRENT_USER);
        if(user ==null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        SeverResponse severResponse = iOrderService.queryOrderPayStatus(user.getId(), orderNo);
        if (severResponse.isSuccess()){
            return SeverResponse.createBySuccess(true);
        }
        return SeverResponse.createBySuccess(false);
    }

}
