package com.my.blog.base.service.single;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface IBaseService<T, E> {

	long countByExample(E e);

	long deleteByExample(E e);

	long deleteByPrimaryKey(T t);

	long insert(T t);

	long insertSelective(T t);

	List<T> selectByExample(E e);

	T selectByPrimaryKey(T t);

	int updateByExampleSelective(@Param("record") T t, @Param("example") E e);

	int updateByExample(@Param("record") T t, @Param("example") E e);

	int updateByPrimaryKeySelective(T t);

	int updateByPrimaryKey(T t);
}
