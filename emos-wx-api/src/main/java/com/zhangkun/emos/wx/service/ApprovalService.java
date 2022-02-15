package com.zhangkun.emos.wx.service;

import java.util.ArrayList;
import java.util.HashMap;

public interface ApprovalService {

    public ArrayList searchUserTaskListByPage(HashMap param);

    public void approvalTask(HashMap param);

}
