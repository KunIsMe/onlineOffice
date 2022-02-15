package com.zhangkun.emos.wx.controller;

import com.zhangkun.emos.wx.common.util.R;
import com.zhangkun.emos.wx.controller.form.TestSayHelloForm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/test")
@Api("测试后端接口")
public class TestController {

    @PostMapping("/sayHello")
    @ApiOperation("最简单的测试方法")
    public R sayHello(@Valid @RequestBody TestSayHelloForm form) {
        return R.ok().put("message", "你好，" + form.getName());
    }

    @PostMapping("/addUser")
    @ApiOperation("测试添加用户")
    @RequiresPermissions(value = {"A", "B"}, logical = Logical.OR)
    public R addUser() {
        return R.ok("测试用户添加成功");
    }

}
