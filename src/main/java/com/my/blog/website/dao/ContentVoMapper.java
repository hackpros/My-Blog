package com.my.blog.website.dao;

import com.my.blog.base.mapper.single.BaseMapper;
import com.my.blog.website.constant.ArticleCateEnum;
import com.my.blog.website.modal.Bo.ArchiveBo;
import com.my.blog.website.modal.Vo.ContentVo;
import com.my.blog.website.modal.Vo.ContentVoExample;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

@Component
public interface ContentVoMapper extends BaseMapper<ContentVo, ContentVoExample> {


    int deleteByPrimaryKey(Integer cid);


    List<ContentVo> selectByExampleWithBLOBs(ContentVoExample example);


    ContentVo selectByPrimaryKey(Integer cid);


    int updateByExampleWithBLOBs(@Param("record") ContentVo record, @Param("example") ContentVoExample example);

    int updateByPrimaryKeyWithBLOBs(ContentVo record);

    List<ArchiveBo> findReturnArchiveBo();

    List<ContentVo> findByCatalog(Integer mid);

    @Select("select thumbnail from t_contents where categories =#{cate}")
    List<String> selectUrlAll(@Param("cate") String cate);

}