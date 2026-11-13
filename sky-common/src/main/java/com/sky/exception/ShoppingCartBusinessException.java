package com.sky.exception;





/**
 * 购物车业务异常
 */
public class ShoppingCartBusinessException extends BaseException {
    /**
     * 创建一个购物车业务异常
     * @param msg 提示信息
     */
    public ShoppingCartBusinessException(String msg) {
        super(msg);
    }

}
