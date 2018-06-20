/**
 * 外文影视辅表
 * FilmMapper.java
 * Copyright(C) 2015-2015 xxxxxx公司
 * All rights reserved.
 * -----------------------------------------------
 * 2018-06-11 Created
 */
package com.my.blog.website.dao;

import com.my.blog.base.mapper.single.BaseMapper;
import org.springframework.stereotype.Component;

@Component
public interface FilmMapper<Film,FilmQueryHelper> extends BaseMapper<Film, FilmQueryHelper> {
}