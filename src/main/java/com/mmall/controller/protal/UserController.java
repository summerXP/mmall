package com.mmall.controller.protal;

import com.mmall.common.Conts;
import com.mmall.common.ResponseCode;
import com.mmall.common.SeverResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpSession;

/**
 * Created by Summer on 2017/6/29.
 */

@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService iUserService;


    /**
     *登录校验方法
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    @ResponseBody
    public SeverResponse<User> login(String username, String password, HttpSession session){
        //在这里调用service---->  mybatis->dao

        SeverResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()){//如果登录成功，就把当前用户信息保存在session中
            session.setAttribute(Conts.CURRENT_USER,response.getData());
        }
        return response;
    }


    /**
     * 退出功能
     * @param session
     * @return
     */
    @RequestMapping(value = "logout.do", method = RequestMethod.POST)
    @ResponseBody
    public SeverResponse<String> logout(HttpSession session){
        session.removeAttribute(Conts.CURRENT_USER);
        return SeverResponse.createBySuccess();
    }


    /**
     * 用户注册功能
     * @param user
     * @return
     */
    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    @ResponseBody
    public SeverResponse<String> register(User user){
        return iUserService.register(user);
    }


    /**
     * 在输入框中，失去焦点的时候，
     * 判断用户名和邮件是否存在（通过type来区别是用户名还是邮箱）
     * 防止恶意通过接口直接访问
     *
     * @param str
     * @param type
     * @return
     */
    @RequestMapping(value = "check_valik.do", method = RequestMethod.POST)
    @ResponseBody
    public SeverResponse<String> checkValik(String str,String type){
        return iUserService.checkValik(str,type);
    }


    /**
     * 获取用户信息
     * @param session
     * @return
     */
    @RequestMapping(value = "getuserinfo.do", method = RequestMethod.POST)
    @ResponseBody
    public SeverResponse<User> getUserInfo(HttpSession session){
        User user = (User) session.getAttribute(Conts.CURRENT_USER);
        if (user != null){
            return SeverResponse.createBySuccess(user);
        }

        return SeverResponse.createByErrorMessage("用户未登录，无法获取当前用户信息！！！");
    }


    /**
     * 点击忘记密码？，返回提示问题。
     * @param username
     * @return
     */
    @RequestMapping(value = "forget_get_question.do", method = RequestMethod.POST)
    @ResponseBody
    public SeverResponse<String> forgetgetQuestion(String username){
        return iUserService.selectQuestion(username);
    }


    /**
     * 校验提示问题的答案
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @RequestMapping(value = "forget_check_answer.do", method = RequestMethod.POST)
    @ResponseBody
    public SeverResponse<String> forgetCheckAnswer(String username,String question,String answer){
        return iUserService.checkAnswer(username, question, answer);
    }


    /**
     * 重置密码
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    @RequestMapping(value = "forget_reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public SeverResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        return iUserService.forgetResetPassword(username, passwordNew, forgetToken);
    }


    /**
     * 登录状态下修改密码
     * @param session
     * @param passwordOld
     * @param passwordNew
     * @return
     */
    @RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public SeverResponse<String> resetPassword(HttpSession session,String passwordOld,String passwordNew){
        //获取当前用户
        User user = (User) session.getAttribute(Conts.CURRENT_USER);
        if (user==null){
            return SeverResponse.createByErrorMessage("用户未登录！！！");
        }

        return iUserService.resetPassword(passwordOld,passwordNew,user);
    }


    /**
     * 在登录状态下，修改个人信息
     * @param session
     * @param user
     * @return
     */
    @RequestMapping(value = "update_infomation.do", method = RequestMethod.POST)
    @ResponseBody
    public SeverResponse<User> updateInfoMation(HttpSession session,User user){
        User currentUser = (User) session.getAttribute(Conts.CURRENT_USER);
        if (currentUser == null){
            return SeverResponse.createByErrorMessage("用户未登录！！！");
        }

        //给当前用户设置用户id
        //要修改的信息。不能包括id和用户名。
        //为防止横向越权，把当前用户的id和name一起传过去。后面也不能改变。

        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        SeverResponse<User> response = iUserService.updateInfoMation(user);
        if (response.isSuccess()){
            //修改成功，把数据保存在session中
            session.setAttribute(Conts.CURRENT_USER,response.getData());
        }

        return response;
    }


    /**
     * 获取当前登录用户的信息
     * @param session
     * @return
     */
    @RequestMapping(value = "get_infomation.do", method = RequestMethod.POST)
    @ResponseBody
    public SeverResponse<User> getInfoMation(HttpSession session){
        //如果没有登录，就强制要登录
        User currentUser = (User) session.getAttribute(Conts.CURRENT_USER);
        if (currentUser == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，请登录后查看！！！status=10");
        }

        return iUserService.getUserInfomation(currentUser.getId());
    }



}
