package com.superdog.music_site.common;


public enum ResultCodeEnum {
    SUCCESS(200,"成功"),
    FAILURE(500,"失败")
    ;
    public final Integer code;

    public final String message;

    ResultCodeEnum(Integer code, String message){
        this.code = code;
        this.message = message;
    }
}
