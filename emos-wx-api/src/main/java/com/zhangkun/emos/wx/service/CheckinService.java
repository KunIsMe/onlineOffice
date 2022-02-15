package com.zhangkun.emos.wx.service;

import java.util.ArrayList;
import java.util.HashMap;

public interface CheckinService {

    public String validCanCheckin(int userId);

    public void checkin(HashMap param);

    public void createFaceModel(int userId, String path);

    public HashMap searchTodayCheckin(int userId);

    public long searchCheckinDays(int userId);

    public ArrayList<HashMap> searchWeekCheckin(HashMap param);

    public ArrayList<HashMap> searchMonthCheckin(HashMap param);

}
