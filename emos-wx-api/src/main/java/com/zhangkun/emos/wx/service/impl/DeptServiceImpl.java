package com.zhangkun.emos.wx.service.impl;

import com.zhangkun.emos.wx.db.dao.TbDeptDao;
import com.zhangkun.emos.wx.db.dao.TbUserDao;
import com.zhangkun.emos.wx.db.pojo.TbDept;
import com.zhangkun.emos.wx.exception.EmosException;
import com.zhangkun.emos.wx.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeptServiceImpl implements DeptService {

    @Autowired
    private TbDeptDao deptDao;

    @Autowired
    private TbUserDao userDao;

    @Override
    public List<TbDept> searchAllDept() {
        List<TbDept> list = deptDao.searchAllDept();
        return list;
    }

    @Override
    public void insertDept(String deptName) {
        int row = deptDao.insertDept(deptName);
        if (row != 1) {
            throw new EmosException("添加部门失败");
        }
    }

    @Override
    public void deleteDeptById(int id) {
        long count = userDao.searchUserCountInDept(id);
        if (count > 0) {
            throw new EmosException("部门中有员工，无法删除部门");
        }
        int row = deptDao.deleteDeptById(id);
        if (row != 1) {
            throw new EmosException("删除部门失败");
        }
    }

    @Override
    public void updateDeptById(TbDept dept) {
        int row = deptDao.updateDeptById(dept);
        if (row != 1) {
            throw new EmosException("修改部门失败");
        }
    }
}
