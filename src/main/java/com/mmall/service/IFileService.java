package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by Summer on 2017/7/6.
 * Desc:文件上传的接口
 */
public interface IFileService {

    String upload(MultipartFile file, String path);

}
