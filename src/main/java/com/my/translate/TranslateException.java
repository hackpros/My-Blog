package com.my.translate;

/**
 * <p>Description: 实现dao中的方法</p>
 * <p>Copyright: Copyright (c) 2017</p>
 * <p>Company: jumore</p>
 *
 * @author rongzheng
 * @date 2018/8/31
 */
public class TranslateException  extends  RuntimeException {
    private static final long serialVersionUID = 3676391735019383501L;
    private String code;

    public TranslateException(String msg) {
        super(msg);
    }

    public TranslateException(String msg, Throwable ex) {
        super(msg, ex);
    }

    public String getCode() {
        return this.code;
    }

    public TranslateException(String msg, String code) {
        super(msg);
        this.code = code;
    }

    public TranslateException(String msg, String code, Throwable ex) {
        super(msg, ex);
        this.code = code;
    }
}