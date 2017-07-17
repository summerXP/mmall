package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Conts;
import com.mmall.common.ResponseCode;
import com.mmall.common.SeverResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Summer on 2017/7/5.
 * 后台产品相关的实现类
 */

@Service("iProductService")
public class ProductServiceImpl implements IProductService{

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ICategoryService iCategoryService;


    /**
     * 更新或新增产品
     * @param product
     * @return
     */
    public SeverResponse saveOrUpdateProduct(Product product){
        if (product != null){
            //如果子图不为空
            if (StringUtils.isNotBlank(product.getSubImages())){
                String[] subImageArray = product.getSubImages().split(",");
                if (subImageArray.length > 0){
                    product.setMainImage(subImageArray[0]);
                }
            }

            //如果更新的话，id一定不是空，是前台传进来的
            if (product.getId() != null){
                int rawCount = productMapper.updateByPrimaryKey(product);
                if (rawCount > 0){
                    return SeverResponse.createBySuccessMessage("更新产品成功！！！");
                }
                return SeverResponse.createByErrorMessage("更新产品失败！！！");
            }else{
                int rawCount = productMapper.insert(product);
                if (rawCount > 0){
                    return SeverResponse.createBySuccessMessage("新增产品成功！！！");
                }
                return SeverResponse.createByErrorMessage("新增产品失败！！！");
            }
        }
        return SeverResponse.createByErrorMessage("新增或更新产品参数错误！！！");
    }


    /**
     * 修改产品上下架状态
     * @param productId
     * @param status
     * @return
     */
    public SeverResponse<String> setSaleStatus(Integer productId,Integer status){
        if (productId == null || status == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);

        int rawCount = productMapper.updateByPrimaryKeySelective(product);
        if (rawCount > 0){
            return SeverResponse.createBySuccessMessage("修改产品销售状态成功！！！");
        }
        return SeverResponse.createByErrorMessage("修改产品销售状态失败！！！");
    }


    /**
     * 获取产品信息
     * @param productId
     * @return
     */
    public SeverResponse<ProductDetailVo> manageProductDetail(Integer productId){
        if (productId == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        //根据id查询到产品
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null){
            return SeverResponse.createByErrorMessage("该产品已下架或者删除！！！");
        }

        //如果不为空，就把该产品封装到一个vo对象中,传递到前台展示
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);

        return SeverResponse.createBySuccess(productDetailVo);
    }


    /**
     *
     * @param product
     * @return
     */
    private ProductDetailVo assembleProductDetailVo(Product product){

        ProductDetailVo productDetailVo = new ProductDetailVo();

        productDetailVo.setId(product.getId());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setName(product.getName());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImage(product.getSubImages());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setStatus(product.getStatus());

        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));


        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category == null){
            productDetailVo.setParentCategoryId(0);//默认根节点
        }else{
            productDetailVo.setParentCategoryId(category.getParentId());
        }


        productDetailVo.setCreatTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));

        return productDetailVo;
    }


    /**
     * 获取产品的List
     * 注意：在这个里面，会显示分页，在这里会用到之前的mybatis三剑客之一的分页开源包
     * @param pageNum
     * @param pageSize
     * @return
     */
    public SeverResponse<PageInfo> getProductList(int pageNum,int pageSize){
        //1.startpage----start

        PageHelper.startPage(pageNum, pageSize);

        //2.填充自己的sql查询语句

        List<Product> productList = productMapper.selectList();

        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product productItem : productList){
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }

        //3.pageHelper----收尾

        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVoList);

        return SeverResponse.createBySuccess(pageResult);
    }





    private ProductListVo assembleProductListVo(Product product){

        ProductListVo productListVo = new ProductListVo();

        productListVo.setId(product.getId());
        productListVo.setName(product.getName());

        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        return productListVo;
    }


    /**
     * 搜索产品
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public SeverResponse<PageInfo> searchProduct(String productName, Integer productId,int pageNum,int pageSize){

            PageHelper.startPage(pageNum, pageSize);

            if (StringUtils.isNotBlank(productName)){
                productName = new StringBuilder().append("%").append(productName).append("%").toString();
            }

            List<Product> productList = productMapper.selectByNameAndProductId(productName, productId);

            List<ProductListVo> productListVoList = Lists.newArrayList();
            for(Product productItem : productList){
                ProductListVo productListVo = assembleProductListVo(productItem);
                productListVoList.add(productListVo);
            }

            PageInfo pageResult = new PageInfo(productList);
            pageResult.setList(productListVoList);

            return SeverResponse.createBySuccess(pageResult);
        }







    public SeverResponse<ProductDetailVo> getProductDetail(Integer productId){
        if (productId == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null){
            return SeverResponse.createByErrorMessage("该产品已经下架或删除！！！");
        }

        if (product.getStatus() != Conts.ProsuctSaleStatus.ON_SALE.getCode()){
            //如果目前的产品不在线
            return SeverResponse.createByErrorMessage("该产品已经下架或删除！！！");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);

        return SeverResponse.createBySuccess(productDetailVo);
    }





    public SeverResponse<PageInfo> getProductByKeywordCategory(String keyword,Integer categoryId,int pageNum,int pageSize,String orderBy){

        if (StringUtils.isBlank(keyword) && categoryId == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        List<Integer> categoryIdList = new ArrayList<Integer>();

        if (categoryId != null){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category == null && StringUtils.isBlank(keyword)){
                //没有该分类,并且还没有关键字,这个时候返回一个空的结果集,不报错
                PageHelper.startPage(pageNum, pageSize);

                List<ProductListVo> productListVoList = Lists.newArrayList();

                PageInfo pageInfo = new PageInfo(productListVoList);

                return SeverResponse.createBySuccess(pageInfo);
            }

            categoryIdList = iCategoryService.selectCategoryAndChildrenById(category.getId()).getData();
        }


        if (StringUtils.isNotBlank(keyword)){
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }



        PageHelper.startPage(pageNum, pageSize);
        //排序处理
        if (StringUtils.isNotBlank(orderBy)){
            if (Conts.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
                String[] orderByArr = orderBy.split("_");
                PageHelper.orderBy(orderByArr[0] + " " + orderByArr[1]);
            }
        }


        List<Product> productList = productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(keyword)?null:keyword, categoryIdList.size()==0?null:categoryIdList);

        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product product : productList){
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }

        PageInfo pageinfo = new PageInfo(productList);
        pageinfo.setList(productListVoList);

        return SeverResponse.createBySuccess(pageinfo);
    }





}
