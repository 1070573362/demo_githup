package com.cxwmpt.demo.controller.system;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Sets;
import com.nuotadi.base.AccountUser;
import com.nuotadi.common.enums.CodeEnum;
import com.nuotadi.common.message.ReturnMessage;
import com.nuotadi.common.message.ReturnMessageUtil;
import com.nuotadi.common.utils.MD5Util;
import com.nuotadi.common.utils.RedisKey;
import com.nuotadi.model.system.*;
import com.nuotadi.service.api.system.SysUserRoleService;
import com.nuotadi.service.api.system.SysUserService;
import com.nuotadi.system.SysLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 页面
     *
     * @return
     */
    @RequestMapping("/html/user/page")
    @SysLog("打开用户管理窗口")
    public String page() {
        return "html/systemSetup/userCenter/user/Page";
    }

    /**
     * 新增索引页面
     * @return
     */
    @RequestMapping("/html/user/addPage")
    public String addPage() {
        return "html/systemSetup/userCenter/user/Add";
    }


    @RequestMapping("/html/user/UpdatePassWordPage")
    public String UpdatePassWordPage(Model model) {
        return "html/systemSetup/userCenter/user/UpdatePassWordPage";
    }

    /**
     * TbAdmin加载新增form页面
     */
    @RequestMapping("/html/user/updatePage")

    public String updatePage(Model model, String id, String action) {
        model.addAttribute("action", action);
        String userRoleIds = "";
        if (StringUtils.isNotBlank(id)) {
            model.addAttribute("Entity", sysUserService.getById(id));
            QueryWrapper<SysUserRole> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", id);
            wrapper.eq("delFlag", false);
            List<SysUserRole> sysUserRoleList = sysUserRoleService.list(wrapper);
            for (int i = 0; i < sysUserRoleList.size(); i++) {
                SysUserRole sysUserRole = sysUserRoleList.get(i);
                if (i == sysUserRoleList.size() - 1) {
                    userRoleIds += sysUserRole.getRoleId();
                } else {
                    userRoleIds += sysUserRole.getRoleId() + ",";
                }
            }
        }
        model.addAttribute("userRoleIds",userRoleIds);
        return "html/systemSetup/userCenter/user/Update";
    }


    /**
     * 查询与显示权限信息
     *
     * @param map
     * @return
     */
    @RequestMapping("/api/auth/user/pageList")
    @ResponseBody
    @SysLog("分页查询用户管理信息")
    public ReturnMessage pageList(@RequestParam Map map) {
        if (map.containsKey("page") && map.containsKey("limit")) {
            PageHelper.startPage(Integer.parseInt(map.get("page").toString()), Integer.parseInt(map.get("limit").toString())).setOrderBy("createDate desc ");
        }
        //判断是否有分页数据传过来
        List<SysUser> list = sysUserService.AllList(map);
        PageInfo<SysUser> info = new PageInfo<>(list);
        return ReturnMessageUtil.sucess(info.getList(), (int)info.getTotal());
    }

    @RequestMapping("/api/auth/user/AllList")
    @ResponseBody
    @SysLog("查询用户管理信息")
    public ReturnMessage AllList(@RequestParam Map map) {
        //判断是否有分页数据传过来
        List<SysUser> list = sysUserService.AllList(map);
        return ReturnMessageUtil.sucess(list);
    }




    @RequestMapping("/api/auth/user/delete")
    @ResponseBody
    @SysLog("删除用户信息")
    public ReturnMessage delete(@RequestParam("ids[]") List<String> ids) {
        if (ids.size() <= 0) {
            return ReturnMessageUtil.error(-1, "没有获取到删除的数据");
        }
        for (String data : ids) {
            redisTemplate.delete(RedisKey.getPermissionKey(data));
            sysUserService.removeById(data);
            QueryWrapper<SysUserRole> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", data);
            wrapper.eq("delFlag", false);
            sysUserRoleService.remove(wrapper);
        }
        return ReturnMessageUtil.sucess();
    }


    @RequestMapping("/api/auth/user/save")
    @ResponseBody
    @SysLog("保存用户信息")
    @RequiresPermissions("sys:user:add")
    public ReturnMessage save(@RequestParam("form") String form, @RequestParam("ArrayIds") String ArrayIds, HttpServletRequest request) {
        SysUser sysUser = JSONObject.parseObject(form, SysUser.class);
        List<String> array = JSONObject.parseArray(ArrayIds, String.class);
        String userId = AccountUser.accountUser(request);
        //查询有没有重复的字段
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.eq("account", sysUser.getAccount());
        wrapper.eq("delFlag", false);
        int count = sysUserService.count(wrapper);
        //添加新用户验证loginID是否相同
        if (sysUser.getId() != null && sysUser.getId().length() > 0) {
            //修改
            //原数据库数据
            SysUser old = sysUserService.getById(sysUser.getId());

            if (!sysUser.getAccount().equals(old.getAccount())) {
                if (count != 0) {
                    return ReturnMessageUtil.error(-1, "对不起，你修改的用户登录帐号重复，请重新创建");
                }
            }
            sysUser.setUpdateId(userId);
            if(sysUser.getPassword()!=null&&sysUser.getPassword()!=""){
                sysUser.setPassword(MD5Util.md5(sysUser.getPassword()));
            }else {
                sysUser.setPassword(old.getPassword());
            }
            if (sysUserService.updateById(sysUser)) {
                //删除原来权限
                QueryWrapper<SysUserRole> wrapper1 = new QueryWrapper<>();
                wrapper1.eq("user_id", sysUser.getId());
                wrapper1.eq("delFlag", false);
                sysUserRoleService.remove(wrapper1);
                for (String data : array) {
                    SysUserRole sysUserRole = new SysUserRole();
                    sysUserRole.setUserId(sysUser.getId());
                    sysUserRole.setRoleId(data);
                    sysUserRole.setCreateId(userId);
                    sysUserRoleService.save(sysUserRole);
                }

                Map<String, Object> param = new HashMap<>();
                param.put("id", sysUser.getId());
                param.put("account", sysUser.getAccount());
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

                redisTemplate.opsForHash().put(RedisKey.getPermissionKey(user.getAccount()), "roles", roleNames);
                redisTemplate.opsForHash().put(RedisKey.getPermissionKey(user.getAccount()), "permissions", permissions);

                return ReturnMessageUtil.sucess();
            }
            return ReturnMessageUtil.error(-1, "修改数据失败");
        } else {
            //新增
            if (count != 0) {
                return ReturnMessageUtil.error(-1, "对不起，你创建的用户登录帐号重复，请重新创建");
            }
            sysUser.setPassword(MD5Util.md5(sysUser.getPassword()));
            sysUser.setCreateId(userId);
            if (sysUserService.save(sysUser)) {
                for (String data : array) {
                    SysUserRole sysUserRole = new SysUserRole();
                    sysUserRole.setUserId(sysUser.getId());
                    sysUserRole.setRoleId(data);
                    sysUserRole.setCreateId(userId);
                    sysUserRoleService.save(sysUserRole);
                }
                return ReturnMessageUtil.sucess();
            }
            return ReturnMessageUtil.error(-1, "新增数据失败");
        }
    }

    /**
     * 保存新密码
     * @return
     */
    @RequestMapping("/api/auth/user/saveNewPassword")
    @ResponseBody
    @SysLog("密码重置")
    public ReturnMessage saveNewPassword(String oldPassword, String newPassword, HttpServletRequest request){
        String userId = AccountUser.accountUser(request);
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.eq("id", userId);
        wrapper.eq("delFlag", 0);
        SysUser sysUser = sysUserService.getOne(wrapper);
        if (sysUser == null) {
            return ReturnMessageUtil.error(CodeEnum.USER_IS_NULL);
        }
        //md5比较
        if (!sysUser.getPassword().equals(MD5Util.md5(oldPassword))) {
            return ReturnMessageUtil.error(-1,"您的旧密码和原数据库内容不一样，请重新输入");
        }
        sysUser.setPassword(MD5Util.md5(newPassword));
        //修改密码成功返回修改后的数据
        sysUser.setUpdateId(userId);
        if (sysUserService.updateById(sysUser)) {
            return  ReturnMessageUtil.sucess();
        }
        return ReturnMessageUtil.error(-1,"修改密码失败");
    }

}
