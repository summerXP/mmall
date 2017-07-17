package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.SeverResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVo;

/**
 * Created by Summer on 2017/7/5.
 * 后台产品相关的接口
 */
public interface IProductService {

    //后台相关
    public SeverResponse saveOrUpdateProduct(Product product);

    SeverResponse<String> setSaleStatus(Integer productId,Integer status);

    SeverResponse<ProductDetailVo> manageProductDetail(Integer productId);

    SeverResponse<PageInfo> getProductList(int pageNum, int pageSize);

    SeverResponse<PageInfo> searchProduct(String productName, Integer productId,int pageNum,int pageSize);

//----------------------前台相关------------------------
    SeverResponse<ProductDetailVo> getProductDetail(Integer productId);

    SeverResponse<PageInfo> getProductByKeywordCategory(String keyword,Integer categoryId,int pageNum,int pageSize,String orderBy);
}
