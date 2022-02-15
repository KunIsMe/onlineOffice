package com.zhangkun.emos.wx.task;

import cn.hutool.core.util.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Scope("prototype")
public class ActiveCodeTask {

    @Autowired
    private EmailTask emailTask;

    @Autowired
    private RedisTemplate redisTemplate;

    @Async
    public void sendActiveCodeAsync(int userId, String email) {
        String activeCode = null;
        // 随机生成6位唯一激活码
        while (true) {
            activeCode = RandomUtil.randomInt(100000, 999999) + "";
            if (redisTemplate.hasKey(activeCode)) {
                continue;
            } else {
                break;
            }
        }
        // 设置激活码有效期为7天
        redisTemplate.opsForValue().set(activeCode, userId + "", 7, TimeUnit.DAYS);
        // 发送邮件
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("“秒见”系统新员工激活码");
        message.setText("您的新员工专属激活码是：" + activeCode + "，有效期为 7 天，请及时前往“秒见”系统进行激活。");
        emailTask.sendAsync(message);
    }

}
