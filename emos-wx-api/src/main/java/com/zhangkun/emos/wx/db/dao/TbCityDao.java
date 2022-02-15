package com.zhangkun.emos.wx.db.dao;

import com.zhangkun.emos.wx.db.pojo.TbCity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TbCityDao {

    public String searchCode(String city);

}