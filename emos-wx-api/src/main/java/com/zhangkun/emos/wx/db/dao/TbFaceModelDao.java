package com.zhangkun.emos.wx.db.dao;

import com.zhangkun.emos.wx.db.pojo.TbFaceModel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TbFaceModelDao {

    public String searchFaceModel(int userId);

    public void insert(TbFaceModel model);

    public int deleteFaceModel(int userId);

}