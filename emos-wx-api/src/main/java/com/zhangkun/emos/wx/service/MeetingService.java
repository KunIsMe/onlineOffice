package com.zhangkun.emos.wx.service;

import com.zhangkun.emos.wx.db.pojo.TbMeeting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface MeetingService {

    public void insertMeeting(TbMeeting entity);

    public ArrayList<HashMap> searchMyMeetingListByPage(HashMap param);

    public HashMap searchMeetingById(int id);

    public void updateMeetingInfo(HashMap param);

    public void deleteMeetingById(int id);

    public void sendResultNotify(String uuid, String result);

    public Long searchRoomIdByUUID(String uuid);

    public List<String> searchUserMeetingInMonth(HashMap param);

}
