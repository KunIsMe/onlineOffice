package com.zhangkun.emos.wx.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.zhangkun.emos.wx.config.SystemConstants;
import com.zhangkun.emos.wx.db.dao.*;
import com.zhangkun.emos.wx.db.pojo.TbCheckin;
import com.zhangkun.emos.wx.db.pojo.TbFaceModel;
import com.zhangkun.emos.wx.exception.EmosException;
import com.zhangkun.emos.wx.service.CheckinService;
import com.zhangkun.emos.wx.task.EmailTask;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Service
@Scope("prototype")
@Slf4j
public class CheckinServiceImpl implements CheckinService {

    @Autowired
    private SystemConstants constants;

    @Autowired
    private TbHolidaysDao holidaysDao;

    @Autowired
    private TbWorkdayDao workdayDao;

    @Autowired
    private TbCheckinDao checkinDao;

    @Autowired
    private TbFaceModelDao faceModelDao;

    @Autowired
    private TbCityDao cityDao;

    @Autowired
    private TbUserDao userDao;

    @Autowired
    private EmailTask emailTask;

    @Value("${emos.face.createFaceModelUrl}")
    private String createFaceModelUrl;

    @Value("${emos.face.checkinUrl}")
    private String checkinUrl;

    @Value("${emos.email.hr}")
    private String hrEmail;

    @Value("${emos.code}")
    private String code;

    @Override
    public String validCanCheckin(int userId) {
        boolean holidays = holidaysDao.searchTodayIsHolidays() != null ? true : false;
        boolean workday = workdayDao.searchTodayIsWorkday() != null ? true : false;
        String type = "?????????";
        if (DateUtil.date().isWeekend()) {
            type = "?????????";
        }
        if (holidays) {
            type = "?????????";
        } else if (workday) {
            type = "?????????";
        }
        if (type.equals("?????????")) {
            return "????????????????????????";
        } else {
            DateTime now = DateUtil.date();
            String start = DateUtil.today() + " " + constants.attendanceStartTime;
            String end = DateUtil.today() + " " + constants.attendanceEndTime;
            DateTime attendanceStart = DateUtil.parse(start);
            DateTime attendanceEnd = DateUtil.parse(end);
            if (now.isBefore(attendanceStart)) {
                return "??????????????????????????????";
            } else if (now.isAfter(attendanceEnd)) {
                return "?????????????????????????????????";
            } else {
                HashMap map = new HashMap();
                map.put("userId", userId);
                map.put("start", attendanceStart);
                map.put("end", attendanceEnd);
                boolean result = checkinDao.haveCheckin(map) != null ? true : false;
                return result ? "????????????????????????????????????????????????" : "??????????????????";
            }
        }
    }

    @Override
    public void checkin(HashMap param) {
        Date now = DateUtil.date();
        Date work = DateUtil.parse(DateUtil.today() + " " + constants.attendanceTime);
        Date workEnd = DateUtil.parse(DateUtil.today() + " " + constants.attendanceEndTime);
        int status = 1; //1??????????????????2?????????
        if (now.compareTo(work) <= 0) {
            status = 1;
        } else if (now.compareTo(work) > 0 && now.compareTo(workEnd) <= 0) {
            status = 2;
        }
        int userId = (Integer) param.get("userId");
        String faceModel = faceModelDao.searchFaceModel(userId);
        if (faceModel == null) {
            throw new EmosException("?????????????????????");
        } else {
            String path = (String) param.get("path");
            HttpRequest request = HttpUtil.createPost(checkinUrl);
            request.form("photo", FileUtil.file(path), "targetModel", faceModel);
            request.form("code", code);
            HttpResponse response = request.execute();
            if (response.getStatus() != 200) {
                log.error("????????????????????????");
                throw new EmosException("????????????????????????");
            }
            String body = response.body();
            if ("?????????????????????".equals(body) || "???????????????????????????".equals(body)) {
                throw new EmosException(body);
            } else if ("False".equals(body)) {
                throw new EmosException("??????????????????????????????");
            } else if ("True".equals(body)) {
                // ?????????????????????????????????????????????
                int risk = 1; //1???????????????2???????????????3????????????
                String address = (String) param.get("address");
                String city = (String) param.get("city");
                String district = (String) param.get("district");
                if (!StrUtil.isBlank(city) && !StrUtil.isBlank(district)) {
                    String code = cityDao.searchCode(city);
                    try {
                        String url = "http://m." + code + ".bendibao.com/news/yqdengji/?qu=" + district;
                        Document document = Jsoup.connect(url).get();
                        Elements elements = document.getElementsByClass("list-content");
                        if (elements.size() > 0) {
                            Element element = elements.get(0);
                            String result = element.select("p:last-child").text();
//                            result = "?????????";
                            if ("?????????".equals(result)) {
                                risk = 3;
                                // ??????????????????
                                HashMap<String, String> map = userDao.searchNameAndDept(userId);
                                String name = map.get("name");
                                String deptName = map.get("dept_name");
                                deptName = deptName != null ? deptName : "";
                                SimpleMailMessage message = new SimpleMailMessage();
                                message.setTo(hrEmail);
                                message.setSubject("?????? " + name + " ?????????????????????????????????");
                                message.setText(deptName + " ?????? " + name + "???" + DateUtil.format(new Date(), "yyyy???MM???dd???") + " ?????? " + address + "????????????????????????????????????????????????????????????????????????????????????");
                                emailTask.sendAsync(message);
                            } else if ("?????????".equals(result)) {
                                risk = 2;
                            }
                        }
                    } catch (Exception e) {
                        log.error("????????????", e);
                        throw new EmosException("??????????????????????????????????????????????????????");
                    }
                }
                // ??????????????????
                String country = (String) param.get("country");
                String province = (String) param.get("province");
                TbCheckin entity = new TbCheckin();
                entity.setUserId(userId);
                entity.setAddress(address);
                entity.setCountry(country);
                entity.setProvince(province);
                entity.setCity(city);
                entity.setDistrict(district);
                entity.setStatus((byte) status);
                entity.setDate(DateUtil.today());
                entity.setCreateTime(now);
                entity.setRisk(risk);
                checkinDao.insert(entity);
            }
        }
    }

    @Override
    public void createFaceModel(int userId, String path) {
        HttpRequest request = HttpUtil.createPost(createFaceModelUrl);
        request.form("photo", FileUtil.file(path));
        request.form("code", code);
        HttpResponse response = request.execute();
        String body = response.body();
        if ("?????????????????????".equals(body) || "???????????????????????????".equals(body)) {
            throw new EmosException(body);
        } else {
            TbFaceModel entity = new TbFaceModel();
            entity.setUserId(userId);
            entity.setFaceModel(body);
            faceModelDao.insert(entity);
        }
    }

    @Override
    public HashMap searchTodayCheckin(int userId) {
        HashMap map = checkinDao.searchTodayCheckin(userId);
        return map;
    }

    @Override
    public long searchCheckinDays(int userId) {
        long days = checkinDao.searchCheckinDays(userId);
        return days;
    }

    @Override
    public ArrayList<HashMap> searchWeekCheckin(HashMap param) {
        ArrayList<HashMap> checkinList = checkinDao.searchWeekCheckin(param);
        ArrayList holidaysList = holidaysDao.searchHolidaysInRange(param);
        ArrayList workdayList = workdayDao.searchWorkdayInRange(param);
        DateTime startDate = DateUtil.parseDate(param.get("startDate").toString());
        DateTime endDate = DateUtil.parseDate(param.get("endDate").toString());
        DateRange range = DateUtil.range(startDate, endDate, DateField.DAY_OF_MONTH);
        ArrayList<HashMap> list = new ArrayList<>();
        range.forEach(value -> {
            String date = value.toString("yyyy-MM-dd");
            String type = "?????????";
            if (value.isWeekend()) {
                type = "?????????";
            }
            if (holidaysList != null && holidaysList.contains(date)) {
                type = "?????????";
            } else if (workdayList != null && workdayList.contains(date)) {
                type = "?????????";
            }
            String status = "";
            if (type.equals("?????????") && DateUtil.compare(value, DateUtil.date()) <= 0) {
                status = "??????";
                boolean flag = false;
                for (HashMap<String, String> map:checkinList) {
                    if (map.containsValue(date)) {
                        status = map.get("status");
                        flag = true;
                        break;
                    }
                }
                DateTime endTime = DateUtil.parse(DateUtil.today() + " " + constants.attendanceEndTime);
                String today = DateUtil.today();
                if (date.equals(today) && DateUtil.date().isBefore(endTime) && flag == false) {
                    status = "";
                }
            }
            HashMap map = new HashMap();
            map.put("date",date);
            map.put("status", status);
            map.put("type", type);
            map.put("day", value.dayOfWeekEnum().toChinese("???"));
            list.add(map);
        });
        return list;
    }

    @Override
    public ArrayList<HashMap> searchMonthCheckin(HashMap param) {
        return this.searchWeekCheckin(param);
    }
}
