package com.mmall.controller.backend;

import com.mmall.common.Conts;
import com.mmall.common.ResponseCode;
import com.mmall.common.SeverResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by Summer on 2017/7/3.
 */
@Controller
@RequestMapping("/manage/category")
public class CategoryManagerController {


    @Autowired
    private IUserService iUserService;
    @Autowired
    private ICategoryService iCategoryService;


    /**
     * 后台管理员添加分类
     * @param session
     * @param categoryName
     * @param parentId
     * @return
     */
    @RequestMapping(value = "add_category.do",method = RequestMethod.POST)
    @ResponseBody
    public SeverResponse addCategory(HttpSession session,String categoryName, @RequestParam(value = "parentId",defaultValue = "0") int parentId){
        User user = (User) session.getAttribute(Conts.CURRENT_USER);
        if(user == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，请登陆后尝试！！！");
        }
        //检验当前用户是否是管理员
        SeverResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            //成功，说明就是管理员
           //Todo 在这里写添加分类的代码
            return iCategoryService.addCategory(categoryName, parentId);

        }else{
            return SeverResponse.createByErrorMessage("当前用户不是管理员，无权限操作！！！");
        }

    }


    /**
     * 后台管理员修改分类名称
     * @param session
     * @param categoryId
     * @param categoryName
     * @return
     */
    @RequestMapping(value = "set_category_name.do",method = RequestMethod.POST)
    @ResponseBody
    public SeverResponse setCategoryName(HttpSession session,Integer categoryId,String categoryName){

        User user = (User) session.getAttribute(Conts.CURRENT_USER);
        if (user == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，请登陆后尝试！！！");
        }
        //校验当前用户是否是管理员
        SeverResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            //成功，说明就是管理员
            //Todo 在这里写修改分类名称的代码
            return iCategoryService.updateCategoryName(categoryId,categoryName);
        }else{
            return SeverResponse.createByErrorMessage("当前用户不是管理员，无权限操作！！！");
        }
    }


    /**
     * 查询子节点的category信息，并且不递归，保持平级
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping(value = "get_category.do",method = RequestMethod.POST)
    @ResponseBody
    public SeverResponse getChildrenParallelCategory(HttpSession session, @RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        User user = (User) session.getAttribute(Conts.CURRENT_USER);
        if (user == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，请登陆后尝试！！！");
        }
        //校验当前用户是否是管理员
        SeverResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            //查询子节点的category信息，并且不递归，保持平级
            //Todo 在这里写查询子节点的category信息的代码
            return iCategoryService.getChildrenParallelCategory(categoryId);

        }else{
            return SeverResponse.createByErrorMessage("当前用户不是管理员，无权限操作！！！");
        }
    }


    /**
     * 递归查询节点下面的所有子节点
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping(value = "get_deep_category.do",method = RequestMethod.POST)
    @ResponseBody
    public SeverResponse getCategoryAndDeepChildrenCategory(HttpSession session,@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        User user = (User) session.getAttribute(Conts.CURRENT_USER);
        if (user == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，请登陆后尝试！！！");
        }
        //校验当前用户是否是管理员
        SeverResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            //递归查询子节点下面所有的category信息，递归
            //Todo 在这里写查询id下面所有的子节点
            return iCategoryService.selectCategoryAndChildrenById(categoryId);
        }else{
            return SeverResponse.createByErrorMessage("当前用户不是管理员，无权限操作！！！");
        }
    }



}
