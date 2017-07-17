package com.mmall.service.impl;

import com.mmall.common.Conts;
import com.mmall.common.SeverResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by Summer on 2017/6/29.
 * Desc:用户模块service接口的实现类
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;


    /*
    这个方法用来校验登录
     */
    @Override
    public SeverResponse<User> login(String username, String password) {

        //查找用户名是否存在
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return SeverResponse.createByErrorMessage("用户名不存在！！！");
        }


        //todo 校验MD5加密密码
        String md5password = MD5Util.MD5EncodeUtf8(password);
        //查找用户名和密码对应的用户
        User user = userMapper.selectLogin(username, md5password);
        if (user == null) {
            return SeverResponse.createByErrorMessage("密码错误！！！");
        }

        //到这里说明用户和密码正确登录成功
        user.setPassword(StringUtils.EMPTY);
        return SeverResponse.createBySuccess("登录成功！！！", user);
    }


    /**
     * 用户注册校验
     *
     * @param user
     * @return
     */
    public SeverResponse<String> register(User user) {
        //校验username和email是否存在

        //查找用户名是否存在
        SeverResponse checkedvalue = this.checkValik(user.getUsername(), Conts.USERNAME);
        if (!checkedvalue.isSuccess()){
            return checkedvalue;
        }
        //查找email是否存在
        checkedvalue = this.checkValik(user.getEmail(),Conts.EMAIL);
        if(!checkedvalue.isSuccess()){
            return checkedvalue;
        }

        //设置此帐号为普通用户
        user.setRole(Conts.Role.ROLE_CUSTOM);
        //用户密码加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        //把当前用户插入到数据库中
        int resultCount = userMapper.insert(user);
        if (resultCount == 0) {
            return SeverResponse.createByErrorMessage("注册失败！！！");
        }
        return SeverResponse.createBySuccessMessage("注册成功！！！");
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
    public SeverResponse<String> checkValik(String str, String type) {
        //先判断type是否为空
        if (StringUtils.isNotBlank(type)) {
            //开始校验
            if (Conts.USERNAME.equals(type)) {//如果type是用户名
                //查找用户名是否存在
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return SeverResponse.createByErrorMessage("用户名已存在！！！");
                }
            }

            if (Conts.EMAIL.equals(type)) {//如果type是邮箱
                //查找email是否存在
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0) {
                    return SeverResponse.createByErrorMessage("邮箱以被注册过！！！");
                }
            }
        } else {
            return SeverResponse.createByErrorMessage("参数类型错误！！！");
        }

        return SeverResponse.createBySuccessMessage("校验成功！！！");
    }


    /**
     * 通过点击忘记密码，获取密码提示问题
     * @param username
     * @return
     */
    public SeverResponse selectQuestion(String username){

        SeverResponse valik = this.checkValik(username, Conts.USERNAME);
        //注意：上面的这个方法得到的结果是：不存在的时候，是校验成功。
        if(valik.isSuccess()){
            //用户不存在
            return SeverResponse.createByErrorMessage("用户不存在！！！");
        }

        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)){
            return SeverResponse.createBySuccess(question);
        }

        return SeverResponse.createByErrorMessage("找回密码的提示问题是空的！！！");
    }


    /**
     * 校验密码提示问题的答案
     * @param username
     * @param question
     * @param answer
     * @return
     */
    public SeverResponse<String> checkAnswer(String username,String question,String answer){
        int result = userMapper.checkAnswer(username, question, answer);
        if (result > 0){
            //说明当前用户的问题和答案都是正确的
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);

            //校验成功，把瓜娃缓存的token保存起来
            return SeverResponse.createBySuccess(forgetToken);
        }

        return SeverResponse.createByErrorMessage("问题的答案错误！！！");
    }


    /**
     * 修改密码
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    public SeverResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        if (StringUtils.isBlank(forgetToken)){
            return SeverResponse.createByErrorMessage("参数错误，token参数要传递！！！");
        }

        SeverResponse valik = this.checkValik(username, Conts.USERNAME);
        //注意：上面的这个方法得到的结果是：不存在的时候，是校验成功。
        if(valik.isSuccess()){
            //用户不存在
            return SeverResponse.createByErrorMessage("用户不存在！！！");
        }

        //获取token
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if (StringUtils.isBlank(token)){
            return SeverResponse.createByErrorMessage("token无效或者过期！！！");
        }

        //比较是否一致
        if (StringUtils.equals(forgetToken,token)){

            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username,md5Password);
            if(rowCount > 0){
                return SeverResponse.createBySuccessMessage("修改密码成功！！！");
            }
        }else{
            return SeverResponse.createByErrorMessage("token获取失败，请重新获取重置密码的token！！！");
        }
        return SeverResponse.createByErrorMessage("修改密码失败！！！");
    }


    /**
     * 用户在登录状态下修改密码
     * @param passwordOld
     * @param passwordNew
     * @param user
     * @return
     */
    public SeverResponse<String> resetPassword(String passwordOld,String passwordNew,User user){
        //防止横向越权,要校验一下这个用户的旧密码,一定要指定是这个用户.因为我们会查询一个count(1),如果不指定id,那么结果就是true啦count>0;

        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if (resultCount == 0){
            return SeverResponse.createByErrorMessage("旧密码错误！！！");
        }


        //如果旧密码正确，则设置新密码
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount > 0){
            return SeverResponse.createBySuccessMessage("密码更新成功！！！");
        }

        return SeverResponse.createByErrorMessage("密码更新失败！！！");

    }


    /**
     * 用户在登录状态下更新个人信息
     * @param user
     * @return
     */
    public SeverResponse<User> updateInfoMation(User user){
        //username是不能被更新的
        //email也要进行一个校验,校验新的email是不是已经存在,并且存在的email如果相同的话,不能是我们当前的这个用户的.
        int resultCont = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCont > 0){
            //说明此email已经存在，并且不是当前用户的
            return SeverResponse.createByErrorMessage("此email已经被注册，请更换email并尝试！！！");
        }

        //声明一个新的user对象，来保存要更新的新信息
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        //更新
        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount > 0){
            //如果成功，就把当前用户修改的保存起来
            return SeverResponse.createBySuccess("个人信息更新成功！！！",updateUser);
        }

        return SeverResponse.createByErrorMessage("更新失败！！！");
    }


    /**
     * 获取当前登录用户个人信息
     * @param userid
     * @return
     */
    public SeverResponse<User> getUserInfomation(Integer userid){
        User user = userMapper.selectByPrimaryKey(userid);
        if (user == null){
            return SeverResponse.createByErrorMessage("找不到当前用户！！！");
        }
        user.setPassword(StringUtils.EMPTY);

        return SeverResponse.createBySuccess(user);
    }






    //backend检验后台的方法

    public SeverResponse checkAdminRole(User user){
            //如果用户不是空，并且是管理员的角色
        if (user != null && user.getRole() == Conts.Role.ROLE_ADMIN) {
            return SeverResponse.createBySuccess();
        }
        return SeverResponse.createByError();
    }






}
