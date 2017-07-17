package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by Summer on 2017/7/6.
 * Decs：文件上传接口的实现类
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService{

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);


    /**
     * 文件上传
     * @param file
     * @param path
     * @return
     */
    public String upload(MultipartFile file,String path){

        //源文件名
        String fileName = file.getOriginalFilename();
        //获取扩展名（从后面取第一个"."分割）（ + 1是去掉小数点）
        String fileExtenstionName = fileName.substring(fileName.lastIndexOf(".") + 1);
        //新文件名
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtenstionName;
        //打印上传日志
        logger.info("开始上传文件：开始上传文件:{},上传的路劲:{},上传的新文件名:{}",fileName,path,uploadFileName);

        //上传文件的目录
        File fileDir = new File(path);
        if (!fileDir.exists()){//如果文件夹不存在，就创建一个
            //设置文件（夹）属性可修改
            fileDir.setWritable(true);
            //创建文件夹
            fileDir.mkdirs();
        }

        //完整的目标文件和路径
        File targetFile = new File(path,uploadFileName);

        try {
            file.transferTo(targetFile);
            //文件上传成功

            //  把文件上传到ftp服务器中
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            
            // TODO: 2017/7/6  上传完成之后，删除upload下面的文件
            targetFile.delete();

        } catch (IOException e) {
            logger.error("文件上传异常！！！",e);
            return null;
        }

        return targetFile.getName();
    }
}
