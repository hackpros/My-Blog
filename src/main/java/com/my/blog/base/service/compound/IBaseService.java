package com.my.blog.base.service.compound;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface IBaseService<T, E, K> {

	long countByExample(E e);

	int deleteByExample(E e);

	int deleteByPrimaryKey(K k);

	long insert(T t);

	long insertSelective(T t);

	List<T> selectByExample(E e);

	T selectByPrimaryKey(K K);

	int updateByExampleSelective(@Param("record") T t, @Param("example") E e);

	int updateByExample(@Param("record") T t, @Param("example") E e);

	int updateByPrimaryKeySelective(K k);

	int updateByPrimaryKey(K k);
}
