package com.mmall.controller.protal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.SeverResponse;
import com.mmall.service.IProductService;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Summer on 2017/7/7.
 * Desc:前台的关于product的控制器
 */
@RequestMapping("/product/")
@Controller
public class ProductController {

    @Autowired
    private IProductService iProductService;


    /**
     * 根据产品id获取产品详情
     * @param productId
     * @return
     */
    @RequestMapping("get_detail.do")
    @ResponseBody
    public SeverResponse<ProductDetailVo> getProductDetail(Integer productId){
        return iProductService.getProductDetail(productId);
    }


    /**
     * 搜索的时候，查询出来的list集合
     * @param keywords
     * @param categoryId
     * @param pageNum
     * @param pageSize
     * @param orderBy
     * @return
     */
    @RequestMapping("get_list.do")
    @ResponseBody
    public SeverResponse<PageInfo> getList(@RequestParam(value = "keywords",required = false) String keywords,
                                           @RequestParam(value = "categoryId",required = false) Integer categoryId,
                                           @RequestParam(value = "pageNum",defaultValue = "1")int pageNum,
                                           @RequestParam(value = "pageSize",defaultValue = "10")int pageSize,
                                           @RequestParam(value = "orderBy",defaultValue = "")String orderBy){

        return iProductService.getProductByKeywordCategory(keywords, categoryId, pageNum, pageSize, orderBy);
    }









}
