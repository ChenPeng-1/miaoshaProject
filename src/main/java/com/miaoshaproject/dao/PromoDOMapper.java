package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.PromoDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
//注意点：当使用mybatis-generator再次生成文件时，就算没有重写，mapper.xml都会重复生成一遍
public interface PromoDOMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PromoDO record);

    int insertSelective(PromoDO record);

    PromoDO selectByPrimaryKey(Integer id);

    PromoDO selectByItemId(Integer itemId);

    int updateByPrimaryKeySelective(PromoDO record);

    int updateByPrimaryKey(PromoDO record);
}