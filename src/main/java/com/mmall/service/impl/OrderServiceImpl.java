package com.mmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Conts;
import com.mmall.common.SeverResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.pojo.Order;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderProductVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import javafx.util.converter.BigDecimalStringConverter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Summer on 2017/7/11.
 * Desc:订单模块接口实现类
 */
@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {



    private static  AlipayTradeService tradeService;
    static {

        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }


    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ShippingMapper shippingMapper;


    /**
     * 创建订单
     * @param userId
     * @param shippingId
     * @return
     */
    public SeverResponse createOrder(Integer userId,Integer shippingId){

        //从购物车中获取数据(把购物车中选中的商品都选出来)
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);

        /*
        计算这个订单的总价
         */
        SeverResponse severResponse = this.getCartOrderItem(userId, cartList);
        if (!severResponse.isSuccess()){
            return severResponse;
        }
        //从返回的结果中拿到数据集合
        List<OrderItem> orderItemList = (List<OrderItem>) severResponse.getData();
        BigDecimal payment = this.getToTalPrice(orderItemList);


        //创建订单
        Order order = this.assembleOrder(userId, shippingId, payment);
        if (order == null){
            return SeverResponse.createByErrorMessage("生成订单错误！！！");
        }

        if (CollectionUtils.isEmpty(orderItemList)){
            return SeverResponse.createByErrorMessage("购物车为空！！！");
        }

        for (OrderItem orderItem : orderItemList){
            orderItem.setOrderNo(order.getOrderNo());
        }

        //批量插入订单
        orderItemMapper.batchinsert(orderItemList);

        //减少（修改）库存数量
        this.reduceProductStock(orderItemList);

        //清空购物车
        this.cleanCart(cartList);

        //给前端返回数据
        OrderVo orderVo = this.assembleOrderVo(order,orderItemList);
        return SeverResponse.createBySuccess(orderVo);
    }


    /**
     * 把信息封装到OrderVo中传递给前台
     * @param order
     * @param orderItemList
     * @return
     */
    private OrderVo assembleOrderVo(Order order,List<OrderItem> orderItemList){
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Conts.PaymentTypeEnum.codeof(order.getPaymentType()).getValues());

        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Conts.OrderStatusEnum.codeof(order.getStatus()).getValues());

        orderVo.setShippingId(order.getShippingId());
        //查询收货地址是否存在
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if (shipping != null){
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(this.assembleShippingVo(shipping));
        }

        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));

        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));


        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        for (OrderItem orderItem : orderItemList){
            OrderItemVo orderItemVo = this.assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }

        orderVo.setOrderItemVoList(orderItemVoList);

        return orderVo;
    }


    /**
     * 重新封装OrderItemVo对象
     * @param orderItem
     * @return
     */
    public OrderItemVo assembleOrderItemVo(OrderItem orderItem) {

        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());
        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));

        return orderItemVo;
    }

    /**
     * 重新封装ShippingVo对象
     * @param shipping
     * @return
     */
    public ShippingVo assembleShippingVo(Shipping shipping){
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        return shippingVo;

    }


    /**
     * 清空购物车
     * @param cartList
     */
    public void cleanCart(List<Cart> cartList){
        for (Cart cart : cartList){
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }


    /**
     * 减少产品库存
     */
    public void reduceProductStock(List<OrderItem> orderItemList){
        for (OrderItem orderItem : orderItemList){
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }


    /**
     * 创建订单
     * @param userId
     * @param shippingId
     * @param payment
     * @return
     */
    public Order assembleOrder(Integer userId,Integer shippingId,BigDecimal payment){

        Order order = new Order();
        //设置订单信息
        //尤其是这个订单号
        long orderNo = this.generateOrderNo();
        order.setOrderNo(orderNo);
        order.setStatus(Conts.OrderStatusEnum.NO_PAY.getCode());
        order.setPostage(0);//运费
        order.setPayment(payment);//设置总价
        //设置付款方式（在线支付或者货到付款）
        order.setPaymentType(Conts.PaymentTypeEnum.ONLINE_PAY.getCode());

        order.setUserId(userId);
        order.setShippingId(shippingId);
        //发货时间等等
        //付款时间等等

        int rawCount = orderMapper.insert(order);
        if (rawCount > 0){
            return order;
        }
        return null;
    }


    /**
     * 生成订单号
     * @return
     */
    private long generateOrderNo(){
        final long currentTime = System.currentTimeMillis();
        /*
        返回的订单号是当前的系统时间加上0/100之间的随机数（
        100%不悔重复，因为这个orderNo是要传给支付宝的，所以绝对不能重复）
        这种形式也不安全，被人看出来每天的订单量

        currentTime + currentTime%10；

         */
        return currentTime + new Random().nextInt(100);
    }


    /**
     * 计算一个订单的总价钱
     * （把一个orderItemList中所有的orderItem里面的totalPrice累加起来）
     * @param orderItemList
     * @return
     */
    public BigDecimal getToTalPrice(List<OrderItem> orderItemList){

        BigDecimal payment = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList){
            payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        return payment;
    }




    /*
    把提出来的商品集合进行封装
    成OrderItemList
    (就像超市的购物条一样，把所有商品集合到一个清单OrderItemList
    清单里面的每一项就是OrderItem（有单价，数量，每个orderitem的总价等）
    每一个orderItem里面有：product的信息)
     */
    public SeverResponse getCartOrderItem(Integer userId,List<Cart> cartList){

        List<OrderItem> orderItemList = Lists.newArrayList();

        if (CollectionUtils.isEmpty(cartList)){
            return SeverResponse.createByErrorMessage("购物车为空！！！");
        }

        //校验购物车的数据,包括产品的状态和数量
        for (Cart cartItem : cartList){

            OrderItem orderItem = new OrderItem();
            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            if (Conts.ProsuctSaleStatus.ON_SALE.getCode() != product.getStatus()){//校验从购物车得到的商品状态和此产品现在的状态
                return SeverResponse.createByErrorMessage("产品" + product.getName() + "不是在售状态！！！");
            }

            //校验库存（校验从购物车中得到的此商品数量和此商品的库存）
            if (cartItem.getQuantity() > product.getStock()){
                return SeverResponse.createByErrorMessage("商品" + product.getName() + "库存不足！！！");
            }

            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartItem.getQuantity()));

            //把orderItem添加到OrderItemList中
            orderItemList.add(orderItem);
        }
        return SeverResponse.createBySuccess(orderItemList);
    }


    /**
     * 取消订单：只能在未付款状态下取消
     * @param userId
     * @param orderNo
     * @return
     */
    public SeverResponse cancel(Integer userId,Long orderNo){

        Order order = orderMapper.selectByUserIdOrderNum(userId, orderNo);
        if (order == null){
            return SeverResponse.createByErrorMessage("该用户此订单不存在！！！");
        }

        if (order.getStatus() != Conts.OrderStatusEnum.NO_PAY.getCode()){
            //如果状态 != 未付款，那就是已经付款了  不能取消
            return SeverResponse.createByErrorMessage("订单已付款，不能取消！！！");
        }

        //如果是未付款，就可以取消
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Conts.OrderStatusEnum.CANCELED.getCode());


        int rawCount = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if (rawCount > 0){
            return SeverResponse.createBySuccess();
        }
        return SeverResponse.createByError();
    }


    /**
     * 获取购物车中选中的
     * @return
     */
    public SeverResponse getOrderCartProduct(Integer userId){
        OrderProductVo orderProductVo = new OrderProductVo();

        //从购物车中获取数据
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        SeverResponse severResponse = this.getCartOrderItem(userId, cartList);
        if (!severResponse.isSuccess()){
            return severResponse;
        }

        List<OrderItem> orderItemList = (List<OrderItem>) severResponse.getData();

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();


        BigDecimal payment = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList){
            payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
            orderItemVoList.add(assembleOrderItemVo(orderItem));
        }


        orderProductVo.setProductTotalPrice(payment);
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return SeverResponse.createBySuccess(orderProductVo);
    }


    /**
     * 获取订单详情
     * @param userId
     * @param orderNo
     * @return
     */
    public SeverResponse getOrderDetail(Integer userId,Long orderNo){
        Order order = orderMapper.selectByUserIdOrderNum(userId, orderNo);
        if (order != null){
            List<OrderItem> orderItemList = orderItemMapper.getByUserIdOrderNum(userId, orderNo);
            OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
            return SeverResponse.createBySuccess(orderVo);
        }
        return SeverResponse.createByErrorMessage("没有找到该订单！！！");
    }


    /**
     * 获取集合
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public SeverResponse<PageInfo> getList(Integer userId,int pageNum,int pageSize){

        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList = assembleOrderVoList(orderList, userId);

        PageInfo pageResult = new PageInfo(orderList);
        pageResult.setList(orderVoList);
        return SeverResponse.createBySuccess(pageResult);
    }


    /*
    把List<Order>转成List<OrderVo>
     */
    private List<OrderVo> assembleOrderVoList(List<Order> orderList,Integer userId){
        List<OrderVo> orderVoList = Lists.newArrayList();
        for (Order order : orderList){
            List<OrderItem> orderItemList = Lists.newArrayList();
            if (userId == null){
                //管理员查询的时候，不用userId
                orderItemMapper.getByOrderNo(order.getOrderNo());
            }else{
                orderItemMapper.getByUserIdOrderNum(userId,order.getOrderNo());
            }

            OrderVo orderVo = assembleOrderVo(order, orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }








    /**
     * 支付订单
     *
     * @param userId
     * @param orderNo
     * @param path
     * @return
     */
    public SeverResponse pay(Integer userId, Long orderNo, String path) {

        Map<String, String> resultMap = Maps.newHashMap();

        Order order = orderMapper.selectByUserIdOrderNum(userId, orderNo);
        if (order == null) {
            return SeverResponse.createByErrorMessage("用户没有该订单！！！");
        }

        resultMap.put("orderNo", String.valueOf(order.getOrderNo()));


        // 测试当面付2.0生成支付二维码
        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("happymmall扫码支付,订单号:").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单").append(outTradeNo).append("购买商品共").append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        List<OrderItem> orderItemList = orderItemMapper.getByUserIdOrderNum(userId, orderNo);

        //把orderItem中的信息映射到GoodsDetail中   在添加到goodsDetailList
        for (OrderItem orderItem : orderItemList) {

            GoodsDetail goods = GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(), new Double(100).doubleValue()).longValue(),
                    orderItem.getQuantity());

            goodsDetailList.add(goods);
        }


//        // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
//        GoodsDetail goods1 = GoodsDetail.newInstance("goods_id001", "xxx小面包", 1000, 1);
//        // 创建好一个商品后添加至商品明细列表
//        goodsDetailList.add(goods1);
//
//        // 继续创建并添加第一条商品信息，用户购买的产品为“黑人牙刷”，单价为5.00元，购买了两件
//        GoodsDetail goods2 = GoodsDetail.newInstance("goods_id002", "xxx牙刷", 500, 2);
//        goodsDetailList.add(goods2);

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);


        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);


                File folder = new File(path);
                if(!folder.exists()){
                    folder.setWritable(true);
                    folder.mkdirs();
                }

                // 需要修改为运行机器上的路径
                //二维码存储路径
                String qrPath = String.format(path + "/qr-%s.png", response.getOutTradeNo());
                //二维码存储名称
                String qrFileName = String.format("qr-%s.png",response.getOutTradeNo());
                //支付宝封装的瓜娃缓存
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);

                File targetFile = new File(path,qrFileName);
                try {
                    //上传二维码到商城服务器
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    logger.error("上传二维码异常",e);
                }

                logger.info("qrPath:" + qrPath);

                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFile.getName();

                resultMap.put("qrUrl",qrUrl);

                return SeverResponse.createBySuccess(resultMap);

            case FAILED:
                logger.error("支付宝预下单失败！！！");
                return SeverResponse.createByErrorMessage("支付宝预下单失败！！！");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知！！！");
                return SeverResponse.createByErrorMessage("系统异常，预下单状态未知！！！");

            default:
                logger.error("不支持的交易状态，交易返回异常！！！");
                return SeverResponse.createByErrorMessage("不支持的交易状态，交易返回异常！！！");
        }

    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }






    public SeverResponse aliCallback(Map<String,String> params){
        //获得商城传递的单号
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        //支付宝交易号
        String tradeNo = params.get("trade_no");
        //目前所处的交易状态
        String trateStatus = params.get("trade_status");

        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null){
            return SeverResponse.createByErrorMessage("不是快乐木商城的订单，回调忽略！！！");
        }

        //如果返回的订单状态是大于支付状态
        if (order.getStatus() >= Conts.OrderStatusEnum.PAID.getCode()){
            return SeverResponse.createBySuccess("支付宝重复调用");
        }

        //如果交易状态是成功，并且支付宝返回的也是成功
        if (Conts.alipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(trateStatus)){
            //订单状态改成已付款
            order.setStatus(Conts.OrderStatusEnum.PAID.getCode());
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            orderMapper.updateByPrimaryKeySelective(order);
        }


        PayInfo payinfo = new PayInfo();
        payinfo.setUserId(order.getUserId());
        payinfo.setOrderNo(order.getOrderNo());
        payinfo.setPayPlatform(Conts.PayPlatFormEnum.ALIPAY.getCode());
        payinfo.setPlatformNumber(tradeNo);
        payinfo.setPlatformStatus(trateStatus);


        payInfoMapper.insert(payinfo);

        return SeverResponse.createBySuccess();
    }


    /**
     * 前台调用这个接口轮番查询   付款是否成功
     * @param userId
     * @param orderNo
     * @return
     */
    public SeverResponse queryOrderPayStatus(Integer userId,Long orderNo){

        Order order = orderMapper.selectByUserIdOrderNum(userId, orderNo);
        if (order == null){
            return SeverResponse.createByErrorMessage("用户没有该订单！！！");
        }

        if (order.getStatus() >= Conts.OrderStatusEnum.PAID.getCode()){
            return SeverResponse.createBySuccess();
        }
        return SeverResponse.createByError();
    }











    //backend后台

    /**
     * 获取订单的集合
     * @param pageNum
     * @param pageSize
     * @return
     */
    public SeverResponse<PageInfo> manageList(int pageNum,int pageSize){
        PageHelper.startPage(pageNum, pageSize);

        List<Order> orderList = orderMapper.selectAllOrder();
        List<OrderVo> orderVoList = assembleOrderVoList(orderList,null);
        PageInfo resultPage = new PageInfo(orderList);
        resultPage.setList(orderVoList);
        return SeverResponse.createBySuccess(resultPage);
    }


    /**
     * 后台获取订单详情
     * @param orderNo
     * @return
     */
    public SeverResponse<OrderVo> manageDetail(Long orderNo){

        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null){
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
            OrderVo orderVo = assembleOrderVo(order,orderItemList);
            return SeverResponse.createBySuccess(orderVo);
        }
        return SeverResponse.createByErrorMessage("订单不存在！！！");
    }


    /**
     * 搜索订单
     * @param orderNo
     * @param pageNum
     * @param pageSize
     * @return
     */
    public SeverResponse<PageInfo> manageSearch(Long orderNo,int pageNum,int pageSize){
        PageHelper.startPage(pageNum, pageSize);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null){
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
            OrderVo orderVo = assembleOrderVo(order,orderItemList);

            PageInfo pageResult = new PageInfo(Lists.newArrayList(order));
            pageResult.setList(Lists.newArrayList(orderVo));
            return SeverResponse.createBySuccess(pageResult);
        }
        return SeverResponse.createByErrorMessage("订单不存在！！！");
    }


    /**
     * 后台管理员发货
     * @param orderNo
     * @return
     */
    public SeverResponse<String> manageSendGoods(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null){
            if (order.getStatus() == Conts.OrderStatusEnum.PAID.getCode()){
                //如果订单在状态是已付款，就可以发货
                order.setStatus(Conts.OrderStatusEnum.SHIPPED.getCode());
                order.setSendTime(new Date());
                orderMapper.updateByPrimaryKeySelective(order);
                return SeverResponse.createBySuccess("发货成功！！！");
            }
        }
        return SeverResponse.createByErrorMessage("订单不存在！！！");
    }









}

