package com.cxwmpt.demo.controller;



import com.cxwmpt.demo.annotation.SysLog;
import com.cxwmpt.demo.common.result.ResultMessage;
import com.cxwmpt.demo.common.util.AesUtil;
import com.cxwmpt.demo.common.util.MD5Util;
import com.cxwmpt.demo.model.system.SysUser;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import static com.cxwmpt.demo.common.util.AesUtil.aesDecrypt;
import static org.apache.shiro.SecurityUtils.getSubject;

/**
 * <p>
 * 登录相关操作
 *
 * @author zhangJian
 * </p>
 */

@Controller
public class AccountController {


    /**
     * 跳转到登录页
     * @return
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/index")
    public String index() {
        SysUser loginUser = (SysUser) getSubject().getPrincipal();
        if(loginUser==null){
            return "login";
        }
        return "index";
    }


    @PostMapping("/api/auth/account/_login")
    @ResponseBody
    @SysLog("用户登录")
    public ResultMessage _login(String userName, String password, boolean remember) throws UnsupportedEncodingException {

        String name = aesDecrypt(userName);
        String pswd = aesDecrypt(password);
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(name, MD5Util.md5(pswd));
        //这边还接收吗异常吗 不用了
            //主体提交登录请求到SecurityManager
        subject.login(token);

        return ResultMessage.success();
    }
    /**
     * 获取当前登录人信息
     * @param
     * @return
     */
    @RequestMapping("/api/auth/account/getLoginInfo")
    @ResponseBody
    public ResultMessage getLoginInfo() {
        SysUser loginUser = (SysUser) getSubject().getPrincipal();
        return ResultMessage.success(loginUser);
    }
    /**
     * 退出
     *
     * @return BJR without data
     */
    @RequestMapping("/api/auth/account/_logout")
   @ResponseBody
    public  ResultMessage _logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Subject subject = getSubject();
        subject.logout(); // session 会销毁，在SessionListener监听session销毁，清理权限缓存
        return ResultMessage.success();
    }
    @RequestMapping("/html/account/home")
    public String home(Model model) {
            SysUser loginUser = (SysUser) getSubject().getPrincipal();

        return "home";
    }
    /**
     * 忘记密码
     * @param model
     * @return
     */
    @RequestMapping("/html/ForgetPassword")
    public String ForgetPassword(Model model) {
        return "html/ForgetPassword";
    }

}
