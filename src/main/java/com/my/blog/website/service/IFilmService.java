package com.my.blog.website.service;

import com.my.blog.base.service.single.IBaseService;

import java.util.List;

/**
 * Created by Administrator on 2017/3/13 013.
 */
public interface IFilmService extends IBaseService {


    List<String> selectUrlAll();
}
