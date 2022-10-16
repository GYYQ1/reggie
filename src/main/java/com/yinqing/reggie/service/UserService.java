package com.yinqing.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yinqing.reggie.common.R;
import com.yinqing.reggie.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public interface UserService extends IService<User> {
    //移动端发送短信验证码
   // public R<String> sendMsg(String email, String code);
    //发送邮件
    void sendMsg(String to,String subject,String text);
}
