package com.zhangkun.emos.wx.db.dao;

import com.zhangkun.emos.wx.db.pojo.TbCheckin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.shiro.crypto.hash.Hash;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbCheckinDao {

    public Integer haveCheckin(HashMap param);

    public void insert(TbCheckin checkin);

    public HashMap searchTodayCheckin(int userId);

    public long searchCheckinDays(int userId);

    public ArrayList<HashMap> searchWeekCheckin(HashMap param);

    public int deleteUserCheckinByUserId(int userId);

}