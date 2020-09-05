package com.cxwmpt.demo.common.result;

/**
 * @program: myp
 * @description
 * @author: YouName
 * @create: 2019-12-12 17:13
 **/
public enum CodeEnum {

    RESOURCESNOTEXISTS(404,"资源不存在"),

    PARAMISNULL(4000,"请求参数为空"),

    USERISNULL(4001,"用户不存在"),

    PASSWORDERROR(4002,"密码不正确"),

    STATUSERROR(4003,"用户状态异常"),

    TOKENEXCEPTION(5000, "token异常"),

    TOKENUNSUPPORTED(5001, "token无效"),

    TOKENMALFORMED(5002, "token格式错误"),

    TOKENSIGNATURE(5003, "token签名无效"),

    TOKENILLEGALARGUMENT(5004, "token参数异常"),

    TOKENEXPIRED(5005, "token已过期"),

    JSONPAESEERROR(6000,"JSON格式错误"),

    NOTPERMISSION(7000,"无权限访问");


    private CodeEnum(Integer code, String msg){
        this.code = code;
        this.msg = msg;
    }
    private Integer code;
    private String msg;

    public Integer getCode() {
        return code;
    }
    public void setCode(Integer code) {
        this.code = code;
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }

}
