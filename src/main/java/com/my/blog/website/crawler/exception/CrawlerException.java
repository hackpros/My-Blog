package com.my.blog.website.crawler.exception;

public class CrawlerException extends RuntimeException {
    private static final long serialVersionUID = 3676391735019383501L;
    private String code;

    public CrawlerException(String msg) {
        super(msg);
    }

    public CrawlerException(String msg, Throwable ex) {
        super(msg, ex);
    }

    public String getCode() {
        return this.code;
    }

    public CrawlerException(String msg, String code) {
        super(msg);
        this.code = code;
    }

    public CrawlerException(String msg, String code, Throwable ex) {
        super(msg, ex);
        this.code = code;
    }
}