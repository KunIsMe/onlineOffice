package com.zhangkun.emos.wx.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhangkun.emos.wx.db.dao.TbMeetingDao;
import com.zhangkun.emos.wx.db.dao.TbUserDao;
import com.zhangkun.emos.wx.db.pojo.MessageEntity;
import com.zhangkun.emos.wx.db.pojo.TbMeeting;
import com.zhangkun.emos.wx.exception.EmosException;
import com.zhangkun.emos.wx.service.MeetingService;
import com.zhangkun.emos.wx.task.MessageTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class MeetingServiceImpl implements MeetingService {

    @Autowired
    private TbMeetingDao meetingDao;

    @Autowired
    private TbUserDao userDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${emos.code}")
    private String code;

    @Value("${workflow.url}")
    private String workflow;

    @Value("${emos.recieveNotify}")
    private String recieveNotify;

    @Autowired
    private MessageTask messageTask;

    private void startMeetingWorkflow(String uuid, int creatorId, String date, String start) {
        HashMap info = userDao.searchUserInfo(creatorId);
        JSONObject json = new JSONObject();
        json.set("url", recieveNotify);
        json.set("uuid", uuid);
        json.set("openId", info.get("openId"));
        json.set("code", code);
        json.set("date", date);
        json.set("start", start);
        String[] roles = info.get("roles").toString().split("???");
        if (!ArrayUtil.contains(roles, "?????????")) {
            Integer managerId = userDao.searchDeptManagerId(creatorId);
            json.set("managerId", managerId);
            Integer gmId = userDao.searchGmId();
            json.set("gmId", gmId);
            boolean bool = meetingDao.searchMeetingMembersInSameDept(uuid);
            json.set("sameDept", bool);
        }
        String url = workflow + "/workflow/startMeetingProcess";
        HttpResponse resp = HttpRequest.post(url).header("Content-Type", "application/json").body(json.toString()).execute();
        if (resp.getStatus() == 200) {
            json = JSONUtil.parseObj(resp.body());
            String instanceId = json.getStr("instanceId");
            HashMap param = new HashMap();
            param.put("uuid", uuid);
            param.put("instanceId", instanceId);
            int row = meetingDao.updateMeetingInstanceId(param);
            if (row != 1) {
                throw new EmosException("???????????????????????????ID??????");
            }
        }
    }

    @Override
    public void insertMeeting(TbMeeting entity) {
        int row = meetingDao.insertMeeting(entity);
        if (row != 1) {
            throw new EmosException("??????????????????");
        }
        // ???????????????????????????
        startMeetingWorkflow(entity.getUuid(), entity.getCreatorId().intValue(), entity.getDate(), entity.getStart());
    }

    @Override
    public ArrayList<HashMap> searchMyMeetingListByPage(HashMap param) {
        ArrayList<HashMap> list = meetingDao.searchMyMeetingListByPage(param);
        String date = null;
        ArrayList resultList = new ArrayList();
        HashMap resultMap = null;
        JSONArray array = null;
        for (HashMap map : list) {
            String temp = map.get("date").toString();
            if (!temp.equals(date)) {
                date = temp;
                resultMap = new HashMap();
                resultMap.put("date", date);
                array = new JSONArray();
                resultMap.put("list", array);
                resultList.add(resultMap);
            }
            array.add(map);
        }
        return resultList;
    }

    @Override
    public HashMap searchMeetingById(int id) {
        HashMap map = meetingDao.searchMeetingById(id);
        ArrayList<HashMap> list = meetingDao.searchMeetingMembers(id);
        map.put("members", list);
        return map;
    }

    @Override
    public void updateMeetingInfo(HashMap param) {
        int id = (int) param.get("id");
        String title = (String) param.get("title");
        String date = param.get("date").toString();
        String start = param.get("start").toString();
        String instanceId = param.get("instanceId").toString();
        HashMap oldMeeting = meetingDao.searchMeetingById(id);
        String uuid = oldMeeting.get("uuid").toString();
        Integer creatorId = Integer.parseInt(oldMeeting.get("creatorId").toString());
        int row = meetingDao.updateMeetingInfo(param);
        if (row != 1) {
            log.error("??????????????????");
            throw new EmosException("??????????????????");
        }
        // ?????????????????????????????????
        JSONObject json = new JSONObject();
        json.set("instanceId" ,instanceId);
        json.set("reason", "???????????????");
        json.set("uuid", uuid);
        json.set("code", code);
        String url = workflow + "/workflow/deleteProcessById";
        HttpResponse resp = HttpRequest.post(url).header("content-type", "application/json").body(json.toString()).execute();
        if (resp.getStatus() != 200) {
            log.error("?????????????????????");
            throw new EmosException("?????????????????????");
        }
        // ????????????????????????????????????
        startMeetingWorkflow(uuid, creatorId, date, start);
    }

    @Override
    public void deleteMeetingById(int id) {
        HashMap meeting = meetingDao.searchMeetingById(id);
        String uuid = meeting.get("uuid").toString();
        String instanceId = meeting.get("instanceId").toString();
        DateTime date = DateUtil.parse(meeting.get("date") + " " + meeting.get("start"));
        DateTime now = DateUtil.date();
        if (now.isAfterOrEquals(date.offset(DateField.MINUTE, -20))) {
            log.error("????????????????????????20????????? ??????????????????");
            throw new EmosException("????????????????????????20????????? ??????????????????");
        }
        int row = meetingDao.deleteMeetingById(id);
        if (row != 1) {
            log.error("??????????????????");
            throw new EmosException("??????????????????");
        }
        // ???????????????????????????
        JSONObject json = new JSONObject();
        json.set("instanceId" ,instanceId);
        json.set("reason", "???????????????");
        json.set("uuid", uuid);
        json.set("code", code);
        String url = workflow + "/workflow/deleteProcessById";
        HttpResponse resp = HttpRequest.post(url).header("content-type", "application/json").body(json.toString()).execute();
        if (resp.getStatus() != 200) {
            log.error("?????????????????????");
            throw new EmosException("?????????????????????");
        }
    }

    @Override
    public void sendResultNotify(String uuid, String result) {
        HashMap meeting = meetingDao.searchMeetingByUuid(uuid);
        String creatorId = meeting.get("creatorId").toString();
        String title = meeting.get("title").toString();
        // ??????????????????????????????RabbitMQ?????????????????????
        MessageEntity entity = new MessageEntity();
        entity.setSenderId(0);
        entity.setSenderName("????????????????????????");
        entity.setUuid(IdUtil.simpleUUID());
        entity.setMsg("???????????????????????? " + title + " ???????????????????????? " + (result.equals("??????") ? "??????" : "?????????"));
        entity.setSendTime(new Date());
        messageTask.sendAsync(creatorId, entity);
    }

    @Override
    public Long searchRoomIdByUUID(String uuid) {
        Object temp = redisTemplate.opsForValue().get(uuid);
        Long roomId = Long.parseLong(temp.toString());
        return roomId;
    }

    @Override
    public List<String> searchUserMeetingInMonth(HashMap param) {
        List<String> list = meetingDao.searchUserMeetingInMonth(param);
        return list;
    }
}
