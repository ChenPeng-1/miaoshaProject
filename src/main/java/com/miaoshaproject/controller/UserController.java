package com.miaoshaproject.controller;

import com.alibaba.druid.util.StringUtils;
import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller("user")//里面的value就是给bean取的名字
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
//@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")//实现跨域请求
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    //用户登录接口
    @RequestMapping(value = "/login",method ={RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telephone")String telephone,
                                  @RequestParam(name = "password")String password) throws BusinessException, NoSuchAlgorithmException {
        if(org.apache.commons.lang3.StringUtils.isEmpty(telephone)
                || org.apache.commons.lang3.StringUtils.isEmpty(password)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        //校验用户登录是否合法
        UserModel userModel = userService.validateLogin(telephone, this.EncodeByMD5(password));

        //采用Token
        String uuidToken = UUID.randomUUID().toString();
        uuidToken.replace("-","");
        redisTemplate.opsForValue().set(uuidToken,userModel);
        redisTemplate.expire(uuidToken,1, TimeUnit.HOURS);

        //将登录凭证加入到用户登录成功的session内(在分布式中用token)
        //this.httpServletRequest.getSession().setAttribute("IS_LOGIN",true);
        //this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);

        return CommonReturnType.create(uuidToken);

    }

    //用户注册接口
    @RequestMapping(value = "/register",method ={RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType register(@RequestParam(name="telephone")String telephone,
                                     @RequestParam(name="otpCode")String otpCode,
                                     @RequestParam(name="name")String name,
                                     @RequestParam(name="gender")Integer gender,
                                     @RequestParam(name="age")Integer age,
                                     @RequestParam(name="password")String password) throws BusinessException, NoSuchAlgorithmException {
        //验证手机号和对应的otpCode是否符合
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telephone);
        if(!StringUtils.equals(otpCode,inSessionOtpCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"短信验证码不符合");
        }
        //用户注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setGender(gender);
        userModel.setAge(age);
        userModel.setTelphone(telephone);
        userModel.setRegisterMode("byphone");
        userModel.setEncrptPassword(this.EncodeByMD5(password));
        userService.register(userModel);
        return CommonReturnType.create(null);
    }

    public String EncodeByMD5(String str) throws NoSuchAlgorithmException {
        //加密方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64en = new BASE64Encoder();
        //加密字符串
        String newstr = base64en.encode(md5.digest(str.getBytes(StandardCharsets.UTF_8)));
        return newstr;
    }
    //用户获取otp短信接口
    // consumes:指定处理请求的提交内容类型（Content-Type），例如application/json, text/html;
    @RequestMapping(value = "/getotp",method ={RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name = "telephone")String telephone)throws Exception{
        //生成opt验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);
        //验证码关联手机号,使用httpsession的方式绑定手机号和验证码（企业中用redis比较好）
        httpServletRequest.getSession().setAttribute(telephone,otpCode);

        //otp验证码通过短信通道发送给用户（省略）
        System.out.println("telephone = " + telephone + "& optCode = " + otpCode);

        return CommonReturnType.create(null);
    }

    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name = "id")Integer id) throws BusinessException {

        UserModel userModel = userService.getUserById(id);
        //如果用户不存在
        if(userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }
        UserVO userVO = convertFromModel(userModel);
        return CommonReturnType.create(userVO);

    }

    private UserVO convertFromModel(UserModel userModel){
        if(userModel == null){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel,userVO);
        return userVO;
    }


}
