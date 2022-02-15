package com.zhangkun.emos.wx.controller;

import cn.hutool.json.JSONUtil;
import com.zhangkun.emos.wx.common.util.R;
import com.zhangkun.emos.wx.config.shiro.JwtUtil;
import com.zhangkun.emos.wx.controller.form.ApprovalTaskForm;
import com.zhangkun.emos.wx.controller.form.SearchUserTaskListByPageForm;
import com.zhangkun.emos.wx.service.ApprovalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/approval")
@Api("审批模块后端接口（前端直接调用）")
public class ApprovalController {

    @Autowired
    private ApprovalService approvalService;

    @PostMapping("/searchUserTaskListByPage")
    @ApiOperation("查询工作流审批任务分页列表")
    @RequiresPermissions(value = {"WORKFLOW:APPROVAL"})
    public R searchUserTaskListByPage(@Valid @RequestBody SearchUserTaskListByPageForm form, @RequestHeader("token") String token) {
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        param.put("token", token);
        ArrayList list = approvalService.searchUserTaskListByPage(param);
        return R.ok().put("result", list);
    }

    @PostMapping("/approvalTask")
    @ApiOperation("审批任务")
    @RequiresPermissions(value = {"WORKFLOW:APPROVAL"})
    public R approvalTask(@Valid @RequestBody ApprovalTaskForm form) {
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        approvalService.approvalTask(param);
        return R.ok().put("result", "success");
    }

}
