package com.mmall.controller.backend;

import com.mmall.common.Conts;
import com.mmall.common.SeverResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by Summer on 2017/6/30.
 * Desc:后台管理员处理类
 */
@Controller
@RequestMapping("/manage/user")
public class UserManagerController {

    @Autowired
    private IUserService iUserService;

    /**
     * 后台管理员登录
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    @ResponseBody
    public SeverResponse<User> login(String username, String password, HttpSession session){

        SeverResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()){
            User user = response.getData();
            if (user.getRole() == Conts.Role.ROLE_ADMIN){
                //如果role的值是1，就是管理员登录
                session.setAttribute(Conts.CURRENT_USER,user);
                return response;
            }else{
                return SeverResponse.createByErrorMessage("当前用户无操作权限，需要管理员权限！！！");
            }
        }
        return response;
    }

}
