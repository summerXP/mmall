package com.mmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.management.monitor.StringMonitor;
import java.io.Serializable;

/**
 * Created by Summer on 2017/6/29.
 * Desc:响应的对象封装，可以指定要返回的泛型类型
 */
/*
这个注释：返回的是序列化json数据是以key:value的格式，比如返回的数据没有data
其实data有key值，但是没有value,这个注释的作用就是过滤没有value的值，key也会消失
不传递到前台。
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class SeverResponse<T> implements Serializable{

    private int status;//指定响应状态
    private String msg;//要返回的信息
    private T data;//响应成功后的泛型数据封装



    //私有构造方法
    private SeverResponse(int code){
        this.status = status;
    }

    private SeverResponse(int status,T date){
        this.status = status;
        this.data = date;
    }

    private SeverResponse(int status,String msg,T data){
        this.status=status;
        this.msg=msg;
        this.data=data;
    }

    private SeverResponse(int status, String msg){
        this.status=status;
        this.msg=msg;
    }


    //对外公开的方法
    //判断是否成功  加上这个注释，它就不会和其他三个参数一起显示
    //不加注释就会一起显示
    @JsonIgnore
    public boolean isSuccess(){
        //判断状态是否和响应成功的code一致
        return this.status == ResponseCode.SUCCESS.getCode();
    }

    public int getStatus(){
        return status;
    }

    public T getData(){
        return data;
    }

    public String getMsg(){
        return msg;
    }


    //只返回状态的实例方法
    public static <T>SeverResponse<T> createBySuccess(){
        return new SeverResponse<T>(ResponseCode.SUCCESS.getCode());
    }

    //返回状态和消息的实例方法
    public static <T>SeverResponse<T> createBySuccessMessage(String msg){
        return new SeverResponse<T>(ResponseCode.SUCCESS.getCode(),msg);
    }

    //返回状态和数据的实例方法
    public static <T>SeverResponse<T> createBySuccess(T data){
        return new SeverResponse<T>(ResponseCode.SUCCESS.getCode(),data);
    }

    //返回状态、信息、数据的实例方法
    public static <T>SeverResponse<T> createBySuccess(String msg,T data){
        return new SeverResponse<T>(ResponseCode.SUCCESS.getCode(),msg,data);
    }




    //出现错误后，返回的数据
    public static <T>SeverResponse<T> createByError(){
        return new SeverResponse<T>(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getDesc());
    }

    public static <T>SeverResponse<T> createByErrorMessage(String errorMessage){
        return new SeverResponse<T>(ResponseCode.ERROR.getCode(),errorMessage);
    }

    //其他错误类型，需要返回的
    public static <T>SeverResponse<T> createByErrorCodeMessage(int errorCode,String errorMessage){
        return new SeverResponse<T>(errorCode,errorMessage);
    }

}
