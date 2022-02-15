package com.zhangkun.emos.wx.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.zhangkun.emos.wx.common.util.R;
import com.zhangkun.emos.wx.config.SystemConstants;
import com.zhangkun.emos.wx.config.shiro.JwtUtil;
import com.zhangkun.emos.wx.controller.form.CheckinForm;
import com.zhangkun.emos.wx.controller.form.SearchMonthCheckinForm;
import com.zhangkun.emos.wx.db.pojo.SysConfig;
import com.zhangkun.emos.wx.db.pojo.TbCheckin;
import com.zhangkun.emos.wx.exception.EmosException;
import com.zhangkun.emos.wx.service.CheckinService;
import com.zhangkun.emos.wx.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@RestController
@RequestMapping("/checkin")
@Api("签到模块后端接口")
@Slf4j
public class CheckinController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CheckinService checkinService;

    @Autowired
    private UserService userService;

    @Autowired
    private SystemConstants constants;

    @Value("${emos.image-folder}")
    private String imageFolder;

    @GetMapping("/validCanCheckin")
    @ApiOperation("查看用户今天是否可以签到")
    public R validCanCheckin(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        String result = checkinService.validCanCheckin(userId);
        return R.ok(result);
    }

    @PostMapping("/checkin")
    @ApiOperation("签到")
    public R checkin(@Valid CheckinForm form, @RequestParam("photo") MultipartFile file, @RequestHeader("token") String token) {
        if (file == null) {
            return R.error("没有上传文件");
        }
        int userId = jwtUtil.getUserId(token);
        String fileName = file.getOriginalFilename().toLowerCase();
        if (!fileName.endsWith(".jpg")) {
            return R.error("必须提交JPG格式的图片");
        } else {
            String path = imageFolder + '/' + fileName;
            try {
                file.transferTo(Paths.get(path));
                HashMap param = new HashMap();
                param.put("userId", userId);
                param.put("path", path);
                param.put("address", form.getAddress());
                param.put("country", form.getCountry());
                param.put("province", form.getProvince());
                param.put("city", form.getCity());
                param.put("district", form.getDistrict());
                checkinService.checkin(param);
                return R.ok("签到成功");
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new EmosException("图片保存失败");
            } finally {
                FileUtil.del(path);
            }
        }
    }

    @PostMapping("/createFaceModel")
    @ApiOperation("创建人脸模型")
    public R createFaceModel(@RequestParam("photo") MultipartFile file, @RequestHeader("token") String token) {
        if (file == null) {
            return R.error("没有上传文件");
        }
        int userId = jwtUtil.getUserId(token);
        String fileName = file.getOriginalFilename().toLowerCase();
        if (!fileName.endsWith(".jpg")) {
            return R.error("必须提交JPG格式的图片");
        } else {
            String path = imageFolder + '/' + fileName;
            try {
                file.transferTo(Paths.get(path));
                checkinService.createFaceModel(userId, path);
                return R.ok("人脸建模成功");
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new EmosException("图片保存失败");
            } finally {
                FileUtil.del(path);
            }
        }
    }

    @GetMapping("/searchTodayCheckin")
    @ApiOperation("查询用户当日签到数据")
    public R searchTodayCheckin(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        HashMap map = checkinService.searchTodayCheckin(userId);
        map.put("attendanceTime", constants.attendanceTime);
        map.put("closingTime", constants.closingTime);
        long days = checkinService.searchCheckinDays(userId);
        map.put("checkinDays", days);

        // 判断日期是否在员工入职之前
        DateTime hiredate = DateUtil.parse(userService.searchUserHiredate(userId));
        DateTime startDate = DateUtil.beginOfWeek(DateUtil.date());
        if (startDate.isBefore(hiredate)) {
            startDate = hiredate;
        }
        DateTime endDate = DateUtil.endOfWeek(DateUtil.date());
        HashMap param = new HashMap();
        param.put("userId", userId);
        param.put("startDate", startDate.toString());
        param.put("endDate", endDate.toString());
        ArrayList<HashMap> list = checkinService.searchWeekCheckin(param);
        map.put("weekCheckin", list);
        return R.ok().put("result", map);
    }

    @PostMapping("/searchMonthCheckin")
    @ApiOperation("查询用户某月签到数据")
    public R searchMonthCheckin(@Valid @RequestBody SearchMonthCheckinForm form, @RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        DateTime hiredate = DateUtil.parse(userService.searchUserHiredate(userId));
        String month = form.getMonth() < 10 ? "0" + form.getMonth() : form.getMonth().toString();
        DateTime startDate = DateUtil.parse(form.getYear() + "-" + month + "-01");
        if (startDate.isBefore(DateUtil.beginOfMonth(hiredate))) {
            throw new EmosException("只能查询入职之后的考勤数据");
        }
        if (startDate.isBefore(hiredate)) {
            startDate = hiredate;
        }
        DateTime endDate = DateUtil.endOfMonth(startDate);
        HashMap param = new HashMap();
        param.put("userId", userId);
        param.put("startDate", startDate.toString());
        param.put("endDate", endDate.toString());
        ArrayList<HashMap> list = checkinService.searchMonthCheckin(param);
        int normal = 0;
        int late = 0;
        int lack = 0;
        for (HashMap<String, String> map : list) {
            String type = map.get("type");
            String status = map.get("status");
            if ("工作日".equals(type)) {
                if ("正常".equals(status)) {
                    normal++;
                } else if ("迟到".equals(status)) {
                    late++;
                } else if ("缺勤".equals(status)) {
                    lack++;
                }
            }
        }
        return R.ok().put("list", list).put("normal", normal).put("late", late).put("lack", lack);
    }

}
