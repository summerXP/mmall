package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Conts;
import com.mmall.common.ResponseCode;
import com.mmall.common.SeverResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartServicr;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Summer on 2017/7/7.
 * Desc:购物车接口的实现类
 */
@Service("iCartServicr")
public class CartServiceImpl implements ICartServicr{

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;


    /**
     * 添加购物车模块
     * @param userId
     * @param count
     * @param productId
     * @return
     */
    public SeverResponse<CartVo> add(Integer userId, Integer count, Integer productId){
        if (productId == null || count == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
        if (cart == null){
            //这个产品不在这个购物车里,需要新增一个这个产品的记录
            Cart cartitem = new Cart();
            cartitem.setQuantity(count);
            cartitem.setChecked(Conts.CartCheck.CHECKED);
            cartitem.setProductId(productId);
            cartitem.setUserId(userId);
            //插入购物车
            cartMapper.insert(cartitem);
        }else{
            //这个产品已经在购物车里了.
            //如果产品已存在,数量相加
            count = cart.getQuantity() + count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
     return this.list(userId);
    }


    /**
     * 更新购物车条目
     * @param userId
     * @param productId
     * @param count
     * @return
     */
    public SeverResponse<CartVo> update(Integer userId,Integer productId,Integer count){
        if(productId == null || count == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Cart cart = cartMapper.selectCartByUserIdProductId(userId,productId);

        if (cart != null){
            //说明此商品购物车已经存在 那就只改商品数量
            cart.setQuantity(count);
        }
        //更新购物车
        cartMapper.updateByPrimaryKey(cart);

        return this.list(userId);
    }


    /**
     * 删除购物车
     * @param userId
     * @param productIds
     * @return
     */
    public SeverResponse<CartVo> deleteProduct(Integer userId,String productIds) {

        List<String> productList = Splitter.on(",").splitToList(productIds);

        if (CollectionUtils.isEmpty(productList)){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        cartMapper.deleteByUserIdProductIds(userId,productList);

        return this.list(userId);

    }


    /**
     * 查询购物车
     * @param userId
     * @return
     */
    public SeverResponse<CartVo> list(Integer userId){
        CartVo cartVo = this.getCartVoLimit(userId);
        return SeverResponse.createBySuccess(cartVo);
    }





    /**
     * 全选或者不选
     * @param userId
     * @param productId
     * @param checked
     * @return
     */
    public SeverResponse<CartVo> selectOrUnSelect (Integer userId,Integer productId,Integer checked){
        cartMapper.checkedOrUncheckedProduct(userId,productId,checked);
        return this.list(userId);
    }


    /**
     * 获取购物车数量
     * @param userId
     * @return
     */
    public SeverResponse<Integer> getCartProductCount(Integer userId){
        if(userId == null){
            return SeverResponse.createBySuccess(0);
        }
        return SeverResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }






















    private CartVo getCartVoLimit(Integer userId){

        CartVo cartVo = new CartVo();

        List<Cart> cartList = cartMapper.selectCartListByUserId(userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();

        BigDecimal cartTotalPrice = new BigDecimal("0");

        if (CollectionUtils.isNotEmpty(cartList)){
            for (Cart cartItem : cartList){
                CartProductVo cartProductVo = new CartProductVo();

                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserid(userId);
                cartProductVo.setProductId(cartItem.getProductId());


                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if (product != null){
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitlet(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());

                    //判断库存
                    int buyLimitCount = 0;
                    if (product.getStock() >= cartItem.getQuantity()){
                        //如果库存大于需求
                        buyLimitCount = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Conts.CartCheck.LIMIT_NUM_SUCCESS);
                    }else{
                        //如果需求大于库存，那就只能显示最大库存，不能在往上累加
                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Conts.CartCheck.LIMIT_NUM_FAIL);

                        //购物车中更新有效库存
                        Cart cartForQuantity = new Cart();
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                    }

                    cartProductVo.setQuantity(buyLimitCount);

                    // TODO: 2017/7/7 计算总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartItem.getQuantity()));

                    //设置默认勾选
                    cartProductVo.setProductChecked(cartItem.getChecked());
                }

                if (cartItem.getChecked() == Conts.CartCheck.CHECKED){
                    //如果已经勾选,增加到整个的购物车总价中
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());
                }

                cartProductVoList.add(cartProductVo);
            }
        }

        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return cartVo;
    }





    //是否全选
    private boolean getAllCheckedStatus(Integer userId){
        if (userId == null){
            return false;
        }
        return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;
    }
}
