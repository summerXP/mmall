package com.mmall.service;

import com.mmall.common.SeverResponse;
import com.mmall.pojo.User;

/**
 * Created by Summer on 2017/6/29.
 * Desc:用户模块的Service接口，扩展性强
 */
public interface IUserService {
    SeverResponse<User> login(String username, String password);

    SeverResponse<String> register(User user);

    SeverResponse<String> checkValik(String str,String type);

    SeverResponse selectQuestion(String username);

    SeverResponse<String> checkAnswer(String username,String question,String answer);

    SeverResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken);

    SeverResponse<String> resetPassword(String passwordOld,String passwordNew,User user);

    SeverResponse<User> updateInfoMation(User user);

    SeverResponse<User> getUserInfomation(Integer userid);

    SeverResponse checkAdminRole(User user);
}
