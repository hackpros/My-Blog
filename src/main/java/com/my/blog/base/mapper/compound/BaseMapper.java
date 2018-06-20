package com.my.blog.base.mapper.compound;

import java.util.List;

import org.apache.ibatis.annotations.Param;
/**
 * 复合主键 基类
 * @author fanwg
 *
 * @param <T>
 * @param <E>
 * @param <K>
 */
public interface BaseMapper<T, E, K> {

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

	List<T> queryForList(E e, int start, int end);

	List<T> selectOnPage(@Param("example") E example,
			@Param("skipResults") int skipResults,
			@Param("maxResults") int maxResults);

}
