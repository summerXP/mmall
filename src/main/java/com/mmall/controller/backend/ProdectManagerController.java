package com.mmall.controller.backend;

import com.google.common.collect.Maps;
import com.mmall.common.Conts;
import com.mmall.common.ResponseCode;
import com.mmall.common.SeverResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Summer on 2017/7/5.
 * Desc:后台产品管理
 */
@RequestMapping("/manage/product")
@Controller
public class ProdectManagerController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private IProductService iProductService;


    @Autowired
    private IFileService iFileService;


    /**
     * 后台增加产品
     * @param session
     * @param product
     * @return
     */
    @RequestMapping(value = "product_save.do")
    @ResponseBody
    public SeverResponse prodcutSave(HttpSession session, Product product){
        User user = (User) session.getAttribute(Conts.CURRENT_USER);
        if (user == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录管理员！！！");
        }

        //判断是不是管理员权限
        if (iUserService.checkAdminRole(user).isSuccess()){
            //填充增加产品的业务逻辑
            return iProductService.saveOrUpdateProduct(product);
        }else{
            return SeverResponse.createByErrorMessage("当前用户不是管理员，没有操作权限！！！");
        }
    }


    /**
     * 修改产品上下架状态的信息
     * @param session
     * @param productId
     * @param status
     * @return
     */
    @RequestMapping(value = "set_sale_status.do")
    @ResponseBody
    public SeverResponse setSaleStatus(HttpSession session, Integer productId,Integer status){
        User user = (User) session.getAttribute(Conts.CURRENT_USER);
        if (user == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录管理员！！！");
        }

        //判断是不是管理员权限
        if (iUserService.checkAdminRole(user).isSuccess()){
            //修改产品销售状态（上下架）
            return iProductService.setSaleStatus(productId, status);
        }else{
            return SeverResponse.createByErrorMessage("当前用户不是管理员，没有操作权限！！！");
        }
    }


    /**
     * 获取产品信息
     * @param session
     * @param productId
     * @return
     */
    @RequestMapping(value = "get_detail_info.do")
    @ResponseBody
    public SeverResponse getDetailInfo(HttpSession session,Integer productId){
        User user = (User) session.getAttribute(Conts.CURRENT_USER);
        if (user == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录管理员！！！");
        }

        //判断是不是管理员权限
        if (iUserService.checkAdminRole(user).isSuccess()){
            //获取产品信息
            return iProductService.manageProductDetail(productId);
        }else{
            return SeverResponse.createByErrorMessage("当前用户不是管理员，没有操作权限！！！");
        }

    }


    /**
     * 获取产品的list
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "get_list.do")
    @ResponseBody
    public SeverResponse getList(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        //1.pageNum:当前页数   2.pageSize：每页的总容量
        User user = (User) session.getAttribute(Conts.CURRENT_USER);
        if (user == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录管理员！！！");
        }
        //判断是不是管理员权限
        if (iUserService.checkAdminRole(user).isSuccess()){
            //填写业务逻辑
            return iProductService.getProductList(pageNum, pageSize);
        }else{
            return SeverResponse.createByErrorMessage("当前用户不是管理员，没有操作权限！！！");
        }

    }


    /**
     * 查询商品
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "product_search.do")
    @ResponseBody
    public SeverResponse productSearch(HttpSession session,String productName,Integer productId, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        //1.pageNum:当前页数   2.pageSize：每页的总容量
        User user = (User) session.getAttribute(Conts.CURRENT_USER);
        if (user == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录管理员！！！");
        }
        //判断是不是管理员权限
        if (iUserService.checkAdminRole(user).isSuccess()){
            //填写业务逻辑
            return iProductService.searchProduct(productName, productId, pageNum, pageSize);
        }else{
            return SeverResponse.createByErrorMessage("当前用户不是管理员，没有操作权限！！！");
        }

    }


    /**
     * 上传文件
     * @param session
     * @param file
     * @param request
     * @return
     */
    @RequestMapping(value = "upload.do")
    @ResponseBody
    public SeverResponse upload(HttpSession session, @RequestParam(value = "upload_file",required = false)MultipartFile file, HttpServletRequest request){

        User user = (User) session.getAttribute(Conts.CURRENT_USER);
        if (user == null){
            return SeverResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录管理员！！！");
        }
        //判断是不是管理员权限
        if (iUserService.checkAdminRole(user).isSuccess()){
            //填写业务逻辑
            String path = request.getSession().getServletContext().getRealPath("upload");

            String targetFileName = iFileService.upload(file,path);

            //获取上传文件保存的路径
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;

            Map fileMap = Maps.newHashMap();
            fileMap.put("uri",targetFileName);
            fileMap.put("url",url);

            return SeverResponse.createBySuccess(fileMap);
        }else{
            return SeverResponse.createByErrorMessage("当前用户不是管理员，没有操作权限！！！");
        }
    }


    /**
     * 富文本文件上传
     * @param session
     * @param file
     * @param request
     * @return
     */
    @RequestMapping(value = "richtext_img_upload.do")
    @ResponseBody
    public Map richtextImgUpload(HttpSession session, @RequestParam(value = "upload_file",required = false)MultipartFile file, HttpServletRequest request, HttpServletResponse response){

        Map resultMap = Maps.newHashMap();

        User user = (User) session.getAttribute(Conts.CURRENT_USER);
        if (user == null){

            resultMap.put("success",false);
            resultMap.put("msg","用户未登录，请登录管理员！！！");
            return resultMap;
        }
        //判断是不是管理员权限
        if (iUserService.checkAdminRole(user).isSuccess()){
            //填写业务逻辑

            //富文本中对于返回值有自己的要求,我们使用是simditor所以按照simditor的要求进行返回
//        {
//            "success": true/false,
//                "msg": "error message", # optional
//            "file_path": "[real file path]"
//        }
            String path = request.getSession().getServletContext().getRealPath("upload");

            String targetFileName = iFileService.upload(file,path);
            if (StringUtils.isBlank(targetFileName)){
                resultMap.put("success",false);
                resultMap.put("msg","上传失败！！！");
                return resultMap;
            }
            //获取上传文件保存的路径
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;

            resultMap.put("success",true);
            resultMap.put("msg","上传成功");
            resultMap.put("file_path",url);
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");
            return resultMap;
        }else{
            resultMap.put("success",false);
            resultMap.put("msg","当前用户不是管理员，没有操作权限！！！");
            return resultMap;
        }
    }







}
