package com.zhangkun.emos.wx.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.zhangkun.emos.wx.common.util.R;
import com.zhangkun.emos.wx.config.shiro.JwtUtil;
import com.zhangkun.emos.wx.config.tencent.TLSSigAPIv2;
import com.zhangkun.emos.wx.controller.form.*;
import com.zhangkun.emos.wx.exception.EmosException;
import com.zhangkun.emos.wx.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Api("用户模块后端接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;

    @Value("${trtc.appid}")
    private Integer appid;

    @Value("${trtc.key}")
    private String key;

    @Value("${trtc.expire}")
    private Integer expire;

    private void saveCacheToken(String token, int userId) {
        redisTemplate.opsForValue().set(token, userId + "", cacheExpire, TimeUnit.DAYS);
    }

    @PostMapping("/register")
    @ApiOperation("注册用户")
    public R register(@Valid @RequestBody RegisterForm form) {
        int userId = userService.registerUser(form.getRegisterCode(), form.getCode(), form.getNickname(), form.getPhoto());
        String token = jwtUtil.createToken(userId);
        Set<String> permissions = userService.searchUserPermissions(userId);
        saveCacheToken(token, userId);
        return R.ok("用户注册成功").put("token", token).put("permission", permissions);
    }

    @PostMapping("/login")
    @ApiOperation("登录系统")
    public R login(@Valid @RequestBody LoginForm form, @RequestHeader("token") String token) {
        Integer userId;
        if (StrUtil.isNotEmpty(token)) {
            try {
                // 判断令牌有效性
                jwtUtil.verifierToken(token);
            } catch (TokenExpiredException e) {
                userId = userService.login(form.getCode());
                token = jwtUtil.createToken(userId);
                saveCacheToken(token, userId);
            }
            userId = jwtUtil.getUserId(token);
        } else {
            userId = userService.login(form.getCode());
            token = jwtUtil.createToken(userId);
            saveCacheToken(token, userId);
        }

        Set<String> permission = userService.searchUserPermissions(userId);
        return R.ok("用户登录成功").put("token", token).put("permission", permission);
    }

    @GetMapping("/searchUserSummary")
    @ApiOperation("查询用户摘要信息")
    public R searchUserSummary(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        HashMap map = userService.searchUserSummary(userId);
        return R.ok().put("result", map);
    }

    @PostMapping("/searchUserGroupByDept")
    @ApiOperation("查询员工列表，按照部门分组排列")
    @RequiresPermissions(value = {"ROOT", "EMPLOYEE:SELECT"}, logical = Logical.OR)
    public R searchUserGroupByDept(@Valid @RequestBody SearchUserGroupByDeptForm form) {
        ArrayList<HashMap> list = userService.searchUserGroupByDept(form.getKeyword());
        return R.ok().put("result", list);
    }

    @PostMapping("/searchMembers")
    @ApiOperation("查询成员")
    @RequiresPermissions(value = {"ROOT", "MEETING:INSERT", "MEETING:UPDATE"}, logical = Logical.OR)
    public R searchMembers(@Valid @RequestBody SearchMembersForm form) {
        if (!JSONUtil.isJsonArray(form.getMembers())) {
            throw new EmosException("members不是JSON数组");
        }
        List param = JSONUtil.parseArray(form.getMembers()).toList(Integer.class);
        ArrayList<HashMap> list = userService.searchMembers(param);
        return R.ok().put("result", list);
    }

    @PostMapping("/selectUserPhotoAndName")
    @ApiOperation("查询用户姓名和头像")
    @RequiresPermissions(value = {"WORKFLOW:APPROVAL"})
    public R selectUserPhotoAndName(@Valid @RequestBody SelectUserPhotoAndNameForm form) {
        if (!JSONUtil.isJsonArray(form.getIds())) {
            throw new EmosException("参数不是JSON数组");
        }
        List<Integer> param = JSONUtil.parseArray(form.getIds()).toList(Integer.class);
        List<HashMap> list = userService.selectUserPhotoAndName(param);
        return R.ok().put("result", list);
    }

    @GetMapping("/genUserSig")
    @ApiOperation("生成用户签名")
    public R genUserSig(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        String email = userService.searchMemberEmail(userId);
        TLSSigAPIv2 api = new TLSSigAPIv2(appid, key);
        String userSig = api.genUserSig(email, expire);
        return R.ok().put("userSig", userSig).put("email", email);
    }

    @PostMapping("/insertUser")
    @ApiOperation("添加员工")
    @RequiresPermissions(value = {"ROOT", "EMPLOYEE:INSERT"}, logical = Logical.OR)
    public R insertUser(@Valid @RequestBody InsertUserForm form) {
        if (!JSONUtil.isJsonArray(form.getRole())) {
            throw new EmosException("角色不是数组格式");
        }
        JSONArray array = JSONUtil.parseArray(form.getRole());
        HashMap param = new HashMap();
        param.put("name", form.getName());
        param.put("sex", form.getSex());
        param.put("tel", form.getTel());
        param.put("email", form.getEmail());
        param.put("hiredate", form.getHiredate());
        param.put("role", form.getRole());
        param.put("deptName", form.getDeptName());
        param.put("status", form.getStatus());
        param.put("createTime", new Date());
        if (array.contains(0)) {
            throw new EmosException("超级管理员为内置角色，不允许被添加");
        } else {
            param.put("root", false);
        }
        userService.insertUser(param);
        return R.ok().put("result", "success");
    }

    @GetMapping("/searchUserSelfInfo")
    @ApiOperation("查询个人信息")
    public R searchUserSelfInfo(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        HashMap map = userService.searchUserInfo(userId);
        map.put("userId", userId);
        return R.ok().put("result", map);
    }

    @PostMapping("/searchUserInfo")
    @ApiOperation("查询员工数据")
    @RequiresPermissions(value = {"ROOT", "EMPLOYEE:SELECT"}, logical = Logical.OR)
    public R searchUserInfo(@Valid @RequestBody SearchUserInfoForm form) {
        HashMap map = userService.searchUserInfo(form.getUserId());
        return R.ok().put("result", map);
    }

    @PostMapping("/updateUserInfo")
    @ApiOperation("修改员工数据")
    @RequiresPermissions(value = {"ROOT", "EMPLOYEE:UPDATE"}, logical = Logical.OR)
    public R updateUserInfo(@Valid @RequestBody UpdateUserInfoForm form) {
        boolean root = false;
        if (!JSONUtil.isJsonArray(form.getRole())) {
            throw new EmosException("角色不是数组格式");
        } else {
            JSONArray role = JSONUtil.parseArray(form.getRole());
            if (role.contains(0)) {
                throw new EmosException("超级管理员为内置角色，不允许被添加");
            }
        }
        HashMap param = new HashMap();
        param.put("userId", form.getUserId());
        param.put("name", form.getName());
        param.put("sex", form.getSex());
        param.put("tel", form.getTel());
        param.put("email", form.getEmail());
        param.put("hiredate", form.getHiredate());
        param.put("role", form.getRole());
        param.put("deptName", form.getDeptName());
        param.put("status", form.getStatus());
        param.put("root", root);
        userService.updateUserInfo(param);
        return R.ok().put("result", "success");
    }

    @PostMapping("/deleteUserById")
    @ApiOperation("删除员工")
    @RequiresPermissions(value = {"ROOT", "EMPLOYEE:DELETE"}, logical = Logical.OR)
    public R deleteUserById(@Valid @RequestBody DeleteUserByIdForm form) {
        userService.deleteUserById(form.getUserId());
        return R.ok().put("result", "success");
    }

    @GetMapping("/searchUserContactList")
    @ApiOperation("查询通讯录列表")
    public R searchUserContactList() {
        JSONObject json = userService.searchUserContactList();
        return R.ok().put("result", json);
    }

}
