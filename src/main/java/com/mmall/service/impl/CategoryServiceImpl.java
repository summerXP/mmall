package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.SeverResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Created by Summer on 2017/7/3.
 * Desc：后台分类Service接口的实现类
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;


    /**
     * 后台添加分类的方法
     * @param categoryName
     * @param parentId
     * @return
     */
    public SeverResponse addCategory(String categoryName,Integer parentId){
        if (parentId == null || StringUtils.isBlank(categoryName)){
            return SeverResponse.createByErrorMessage("添加品类参数错误！！！");
        }

        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);//这个分类是可用的

        //调用mapper里的方法来insert
        int rawCount = categoryMapper.insert(category);
        if (rawCount > 0){
            return SeverResponse.createBySuccessMessage("添加品类成功！！！");
        }

        return SeverResponse.createByErrorMessage("添加品类失败！！！");

    }


    /**
     * 后台修改分类名称的方法
     * @param categoryId
     * @param categoryName
     * @return
     */
    public SeverResponse updateCategoryName(Integer categoryId,String categoryName){
        if(categoryId == null || StringUtils.isBlank(categoryName)){
            return SeverResponse.createByErrorMessage("添加品类参数错误！！！");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);

        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(rowCount > 0){
            return SeverResponse.createBySuccess("更新品类名字成功");
        }
        return SeverResponse.createByErrorMessage("更新品类名字失败");
    }


    /**
     * 获取当前的子节点，但不递归，保持平级
     * @return
     */
    public SeverResponse<List<Category>> getChildrenParallelCategory(Integer categoryId){

        List<Category> categoryList = categoryMapper.selectcategoryChildrenByParentId(categoryId);
        if (CollectionUtils.isEmpty(categoryList)){
            //既有判断为null，又判断了为0个元素的空集合
            logger.info("未找好当前分类的子分类！！！");
        }

        return SeverResponse.createBySuccess(categoryList);
    }


    /**
     * 递归查询本节点的id及孩子节点的id
     * 返回的是下面所有子节点的id的集合
     * @param categoryId
     * @return
     */
    public SeverResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId){
        Set<Category> categorySet = Sets.newHashSet();//初始化
        //调用递归算法 查询
        findChildCategory(categorySet,categoryId);

        List<Integer> categoryList = Lists.newArrayList();
        if (categoryId != null){
            for (Category categoryItem : categorySet){
                categoryList.add(categoryItem.getId());
            }
        }

        return SeverResponse.createBySuccess(categoryList);
    }


    //递归算法,算出子节点
    private Set<Category> findChildCategory(Set<Category> categorySet , Integer categoryId){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if(category != null){
            categorySet.add(category);
        }
        //查找子节点,递归算法一定要有一个退出的条件
        List<Category> categoryList = categoryMapper.selectcategoryChildrenByParentId(categoryId);
        for(Category categoryItem : categoryList){
            findChildCategory(categorySet,categoryItem.getId());
        }
        return categorySet;
    }


}
