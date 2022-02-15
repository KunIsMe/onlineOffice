package com.zhangkun.emos.wx.service;

import com.zhangkun.emos.wx.db.pojo.TbDept;

import java.util.List;

public interface DeptService {

    public List<TbDept> searchAllDept();

    public void insertDept(String deptName);

    public void deleteDeptById(int id);

    public void updateDeptById(TbDept dept);

}
