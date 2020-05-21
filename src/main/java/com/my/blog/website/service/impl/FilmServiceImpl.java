package com.my.blog.website.service.impl;

import com.my.blog.base.service.single.BaseServiceImp;
import com.my.blog.website.dao.FilmMapper;
import com.my.blog.website.service.IFilmService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Administrator on 2017/3/13 013.
 */
@Service
public class FilmServiceImpl extends BaseServiceImp implements IFilmService {

    final FilmMapper filmMapper;

    public FilmServiceImpl(FilmMapper filmMapper) {
        this.filmMapper = filmMapper;
        super.setBaseMapper(filmMapper);
    }


    @Override
    public List<String> selectUrlAll() {
        return filmMapper.selectUrlAll();
    }
}
