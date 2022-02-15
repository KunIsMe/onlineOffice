package com.zhangkun.emos.wx;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.zhangkun.emos.wx.db.pojo.MessageEntity;
import com.zhangkun.emos.wx.db.pojo.MessageRefEntity;
import com.zhangkun.emos.wx.db.pojo.TbMeeting;
import com.zhangkun.emos.wx.service.MeetingService;
import com.zhangkun.emos.wx.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
class EmosWxApiApplicationTests {

    @Autowired
    private MessageService messageService;

    @Autowired
    private MeetingService meetingService;

    @Test
    void contextLoads() {
        for (int i = 1; i <= 100; i++) {
            MessageEntity message = new MessageEntity();
            message.setUuid(IdUtil.simpleUUID());
            message.setSenderId(0);
            message.setSenderName("系统消息");
            message.setMsg("这是第" + i + "条测试消息。");
            message.setSendTime(new Date());
            String id = messageService.insert(message);

            MessageRefEntity refEntity = new MessageRefEntity();
            refEntity.setMessageId(id);
            refEntity.setReceiverId(7);
            refEntity.setLastFlag(true);
            refEntity.setReadFlag(false);
            messageService.insertRef(refEntity);
        }
    }

    @Test
    void createMeetingData() {
        for (int i = 1; i <= 100; i++) {
            TbMeeting meeting = new TbMeeting();
            meeting.setId((long) i);
            meeting.setUuid(IdUtil.simpleUUID());
            meeting.setTitle("测试会议" + i);
            meeting.setCreatorId(7L);
            meeting.setDate(DateUtil.today());
            meeting.setPlace("线上会议室");
            meeting.setStart("09:00");
            meeting.setEnd("11:00");
            meeting.setType((short) 1);
            meeting.setMembers("[7, 8]");
            meeting.setDesc("“秒见”在线协同办公应用系统会议模块测试");
            meeting.setInstanceId(IdUtil.simpleUUID());
            meeting.setStatus((short) 3);
            meetingService.insertMeeting(meeting);
        }
    }

}
