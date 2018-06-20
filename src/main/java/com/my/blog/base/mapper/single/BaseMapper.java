package com.my.blog.base.mapper.single;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface BaseMapper<T, E> {

	int countByExample(E e);

	int deleteByExample(E e);

	int deleteByPrimaryKey(T t);

	long insert(T t);

	long insertSelective(T t);

	List<T> selectByExample(E e);

	T selectByPrimaryKey(T t);

	int updateByExampleSelective(@Param("record") T t, @Param("example") E e);

	int updateByExample(@Param("record") T t, @Param("example") E e);

	int updateByPrimaryKeySelective(T t);

	int updateByPrimaryKey(T t);

	List<T> queryForList(E e, int start, int end);
	
	List<T> selectOnPage(@Param("example")E example, @Param("skipResults")int skipResults,
			@Param("maxResults")int maxResults);

}
