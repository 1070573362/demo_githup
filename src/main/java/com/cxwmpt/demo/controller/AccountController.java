package com.cxwmpt.demo.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Sets;
import com.nuotadi.base.AccountUser;
import com.nuotadi.common.enums.CodeEnum;
import com.nuotadi.common.exception.ApiException;
import com.nuotadi.common.message.ReturnMessage;
import com.nuotadi.common.message.ReturnMessageUtil;
import com.nuotadi.common.utils.JWTUtil;
import com.nuotadi.common.utils.MD5Util;
import com.nuotadi.common.utils.RedisKey;
import com.nuotadi.model.system.SysMenu;
import com.nuotadi.model.system.SysRole;
import com.nuotadi.model.system.SysUser;
import com.nuotadi.service.api.system.SysMenuService;
import com.nuotadi.service.api.system.SysUserService;
import com.nuotadi.system.SysLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.nuotadi.common.utils.AesUtil.aesDecrypt;


/**
 * <p>
 * 登录相关操作
 *
 * @author zhangJian
 * </p>
 */

@Controller
public class AccountController  {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysMenuService sysMenuService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 跳转到登录页
     * @return
     */
    @GetMapping("html/login")
    public String login() {
        return "login";
    }

    @GetMapping("html/index")
    public String index() {
        return "index";
    }


    @PostMapping("/api/_login")
    @ResponseBody
    public ReturnMessage<Object> _login(String userName, String password, boolean remember, String calibrationBit, HttpServletResponse response) throws UnsupportedEncodingException {
        String name = aesDecrypt(userName);
        String pswd = aesDecrypt(password);
        //带有中文md5加密
        if (!MD5Util.md5( URLEncoder.encode(userName + password, "utf-8")).equals(calibrationBit)) {
            System.out.println("检验位错误");
        }

        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(name, MD5Util.md5(pswd));
        try {
            //主体提交登录请求到SecurityManager
            subject.login(token);
        } catch (UnknownAccountException e) {
            return ReturnMessageUtil.error(CodeEnum.USER_IS_NULL);
        }
        return ReturnMessageUtil.sucess();
    }


    @PostMapping("auth")
    @ResponseBody
    @SysLog("用户登录")
    public ReturnMessage<Object> login(String userName, String password , String calibrationBit, HttpServletResponse response) throws UnsupportedEncodingException {

        String name = aesDecrypt(userName);
        String pswd = aesDecrypt(password);
        //带有中文md5加密
        if (!MD5Util.md5( URLEncoder.encode(userName + password, "utf-8")).equals(calibrationBit)) {
            System.out.println("检验位错误");
        }
        //查询用户信息
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.eq("account", name);
        wrapper.eq("delFlag", 0);
        SysUser user = sysUserService.getOne(wrapper);
        //用户不存在
        if (user == null) {
            throw new ApiException(CodeEnum.USER_IS_NULL);
        }
        //密码错误
        if (!user.getPassword().equals(MD5Util.md5(pswd))) {
            throw new ApiException(CodeEnum.PASSWORD_ERROR);
        }

        String token = JWTUtil.generatorToken(user.getId(),
                name,
                24*60*60*1000L);

        return ReturnMessageUtil.sucess(token);
    }

    @GetMapping("/api/auth/isLogin")
    @ResponseBody
    public ReturnMessage<Object> isLogin() {

        return ReturnMessageUtil.sucess("用户已登录");

    }
    @SysLog("用户退出")
    @GetMapping("/api/auth/account/logout")
    public String logout(){
        SecurityUtils.getSubject().logout();
        return "redirect:/login";
    }
    @SysLog("获取当前登录的用户信息")
    @GetMapping("/api/auth/account/getLoginInfo")
    public ReturnMessage getLoginInfo(HttpServletRequest request){
        String userId = AccountUser.accountUser(request);
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.eq("account", userId);
        wrapper.eq("delFlag", false);
        SysUser sysUser = sysUserService.getOne(wrapper);
        //用户不存在
        if (sysUser == null) {
            return ReturnMessageUtil.error(CodeEnum.USER_IS_NULL);
        }
        return ReturnMessageUtil.sucess(sysUser);
    }

    @PostMapping("/api/auth/isPermission")
    @ResponseBody
    public ReturnMessage isPermission(HttpServletRequest request){
        String userId = AccountUser.accountUser(request);
        SysUser us = sysUserService.getById(userId);
        //从缓存中获取授权信息
        Boolean flag = redisTemplate.hasKey(RedisKey.getPermissionKey(us.getId()));
        if (flag) {
            return ReturnMessageUtil.sucess(redisTemplate.opsForHash().get(RedisKey.getPermissionKey(us.getId()),"permissions"));
        }
        Map<String, Object> param = new HashMap<>();
        param.put("id", us.getId());
        param.put("account", us.getAccount());
        SysUser user = sysUserService.selectUserByMap(param);
        Set<SysRole> roles = user.getRoleLists();
        Set<String> roleNames = Sets.newHashSet();
        for (SysRole role : roles) {
            if(StringUtils.isNotBlank(role.getRoleName())){
                roleNames.add(role.getRoleName());
            }
        }
        Set<SysMenu> menus = user.getMenus();
        Set<String> permissions = Sets.newHashSet();
        for (SysMenu menu : menus) {
            if(StringUtils.isNotBlank(menu.getPermission())){
                permissions.add(menu.getPermission());
            }
        }

        redisTemplate.opsForHash().put(RedisKey.getPermissionKey(user.getId()), "roles", roleNames);
        redisTemplate.opsForHash().put(RedisKey.getPermissionKey(user.getId()), "permissions", permissions);

        return ReturnMessageUtil.sucess(permissions);
    }
}
