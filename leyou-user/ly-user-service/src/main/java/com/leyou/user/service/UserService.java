package com.leyou.user.service;


import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/15 16:26
 * @Description:
 */
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "user:verify:phone";

    //校验数据
    public boolean checkData(String data, Integer type) {
        User user = new User();
        switch (type){  //判断数据类型
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnum.INVALID_USER_DATA_TYPE);
        }
        //只需要判断有无，所以采用计数查询效率较高
        return userMapper.selectCount(user) == 0;
    }
    //发送短信验证码
    public void sendCode(String phone) {
        //生成key
        String key = KEY_PREFIX + phone;
        //  生成code
        String code = NumberUtils.generateCode(6);
        HashMap<String, String> msg = new HashMap<>();
        msg.put("phone",phone);
        msg.put("code",code);
        amqpTemplate.convertAndSend("ly.sms.exchange","sms.verify.code",msg);
        //  保存验证码
        redisTemplate.opsForValue().set(key,code,5,TimeUnit.MINUTES);
    }
    //用户注册
    public void register(User user, String code) {
        //校验验证码
        String cacheCode = redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        if (!StringUtils.equals(code,cacheCode)){
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }
        //生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        //对密码进行加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));
        //保存用户
        user.setCreated(new Date());
        int i = userMapper.insertSelective(user);
        if (i < 1){
            throw new LyException(ExceptionEnum.CREATE_USER_ERROR);
        }
    }

    //  根据用户名密码查询用户
    public User queryUserByUsernameAndPassword(String username, String password) {
        //查询用户
        User user = new User();
        user.setUsername(username);
        User user1 = userMapper.selectOne(user);
        //  校验是否有数据
        if (user1 == null) {
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        //校验密码
        if (!StringUtils.equals(CodecUtils.md5Hex(password,user1.getSalt()),user1.getPassword())){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        return user1;
    }
}
