package com.mmall.service;

import com.mmall.common.SeverResponse;
import com.mmall.pojo.Category;

import java.util.List;

/**
 * Created by Summer on 2017/7/3.
 * Desc:后台分类的Service接口
 */
public interface ICategoryService {

    SeverResponse addCategory(String categoryName,Integer parentId);

    SeverResponse updateCategoryName(Integer categoryId,String categoryName);

    SeverResponse<List<Category>> getChildrenParallelCategory(Integer categoryId);

    SeverResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId);
}
