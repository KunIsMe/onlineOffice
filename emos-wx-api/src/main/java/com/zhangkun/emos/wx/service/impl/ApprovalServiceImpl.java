package com.zhangkun.emos.wx.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhangkun.emos.wx.config.shiro.JwtUtil;
import com.zhangkun.emos.wx.exception.EmosException;
import com.zhangkun.emos.wx.service.ApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public class ApprovalServiceImpl implements ApprovalService {

    @Value("${workflow.url}")
    private String workflow;

    @Value("${emos.code}")
    private String code;

    @Override
    public ArrayList searchUserTaskListByPage(HashMap param) {
        param.put("code", code);
        String url = workflow + "/workflow/searchUserTaskListByPage";
        HttpResponse response = HttpRequest.post(url).header("Content-Type", "application/json").body(JSONUtil.toJsonStr(param)).execute();
        if (response.getStatus() == 200) {
            JSONObject json = JSONUtil.parseObj(response.body());
            ArrayList result = json.get("result", ArrayList.class);
            return result;
        } else {
            throw new EmosException("查询工作流审批任务失败");
        }
    }

    @Override
    public void approvalTask(HashMap param) {
        param.put("code", code);
        String url = workflow + "/workflow/approvalMeeting";
        HttpResponse response = HttpRequest.post(url).header("Content-Type", "application/json").body(JSONUtil.toJsonStr(param)).execute();
        if (response.getStatus() != 200) {
            throw new EmosException("审批工作流失败");
        }
    }
}
