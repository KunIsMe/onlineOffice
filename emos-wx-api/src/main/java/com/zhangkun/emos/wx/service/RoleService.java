package com.zhangkun.emos.wx.service;

import com.zhangkun.emos.wx.db.pojo.TbRole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface RoleService {

    public ArrayList<HashMap> searchRoleOwnPermission(int id);

    public ArrayList<HashMap> searchAllPermission();

    public void insertRole(TbRole role);

    public void updateRolePermissions(TbRole role);

    public List<TbRole>  searchAllRole();

    public void deleteRoleById(int id);

}
