package com.my.blog.website.modal.Bo;

import com.my.blog.website.modal.Vo.ContentVo;

import java.io.Serializable;
import java.util.List;

/**
 * Created by 13 on 2017/2/23.
 */
public class ArchiveBo implements Serializable {

    private String date;
    private String count;
    /**
     * 分类查条件
     */
    private String category;

    private List<ContentVo> articles;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public List<ContentVo> getArticles() {
        return articles;
    }

    public void setArticles(List<ContentVo> articles) {
        this.articles = articles;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Archive [" +
                "date='" + date + '\'' +
                ", count='" + count + '\'' +
                ", articles=" + articles +
                ']';
    }
}
