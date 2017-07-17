package com.mmall.common;

/**
 * Created by Summer on 2017/6/29.
 * Desc:响应的时候用到的各种魔鬼数字
 */
public enum ResponseCode {


    //在这里添加各种响应的魔鬼数字
    //以后也是在这里添加
    SUCCESS(0,"SUCCESS"),
    ERROR(1,"ERROR"),
    NEED_LOGIN(10,"NEED_LOGIN"),
    ILLEGAL_ARGUMENT(2,"ILLEGAL_ARGUMENT");



    private final int code;
    private final String desc;

    ResponseCode(int code,String desc){
        this.code=code;
        this.desc=desc;
    }


    public int getCode(){
        return code;
    }

    public String  getDesc(){
        return desc;
    }


}
