package com.cxwmpt.demo.controller.system;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.nuotadi.base.AccountUser;
import com.nuotadi.common.message.ReturnMessage;
import com.nuotadi.common.message.ReturnMessageUtil;
import com.nuotadi.common.utils.RedisKey;
import com.nuotadi.model.system.SysRole;
import com.nuotadi.model.system.SysRoleMenu;
import com.nuotadi.model.system.SysUserRole;
import com.nuotadi.service.api.system.SysRoleMenuService;
import com.nuotadi.service.api.system.SysRoleService;
import com.nuotadi.service.api.system.SysUserRoleService;
import com.nuotadi.system.SysLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
public class SysRoleController {
    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private SysRoleMenuService sysRoleMenuService;
    @Autowired
    private SysUserRoleService sysUserRoleService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 菜单栏页面
     *
     * @return
     */
    @RequestMapping("html/role/page")
    @SysLog("打开角色管理窗口")
    public String page() {
        return "html/systemSetup/userCenter/role/Page";
    }
    @RequestMapping("html/role/addPage")
    public String addPage() {
        return "html/systemSetup/userCenter/role/Add";
    }
    @RequestMapping("html/role/updatePage")
    public String updatePage(Model model, String id, String action)
    { model.addAttribute("action", action);
        String authorityTree = "";
        if (StringUtils.isNotBlank(id)) {
            model.addAttribute("Entity", sysRoleService.getById(id));
            QueryWrapper<SysRoleMenu> wrapper = new QueryWrapper<>();
            wrapper.eq("role_id", id);
            wrapper.eq("delFlag", false);
            List<SysRoleMenu> sysRoleMenus = sysRoleMenuService.list(wrapper);
            for (int i = 0; i < sysRoleMenus.size(); i++) {
                SysRoleMenu sysRoleMenu = sysRoleMenus.get(i);
                if (i == sysRoleMenus.size() - 1) {
                    authorityTree += sysRoleMenu.getMenuId();
                } else {
                    authorityTree += sysRoleMenu.getMenuId() + ",";
                }
            }
        }
        model.addAttribute("authorityTree", authorityTree);
        return "html/systemSetup/userCenter/role/Update";
    }



    /**
     * 查询与显示权限信息
     *
     * @param map
     * @return
     */
    @RequestMapping("/api/auth/role/pageList")
    @ResponseBody
    @SysLog("分页查询角色管理的信息")
    public ReturnMessage pageList(@RequestParam Map map) {
        if (map.containsKey("page") && map.containsKey("limit")) {
            PageHelper.startPage(Integer.parseInt(map.get("page").toString()), Integer.parseInt(map.get("limit").toString())).setOrderBy("createDate desc ");
        }
        //判断是否有分页数据传过来
        List<SysRole> list = sysRoleService.AllList(map);
        PageInfo<SysRole> info = new PageInfo<>(list);
        return ReturnMessageUtil.sucess(info.getList(), (int)info.getTotal());

    }
    @RequestMapping("/api/auth/role/AllList")
    @ResponseBody
    @SysLog("查询角色管理的信息")
    public ReturnMessage AllList(@RequestParam Map map) {
        //判断是否有分页数据传过来
        List<SysRole> list = sysRoleService.AllList(map);
        return ReturnMessageUtil.sucess(list);
    }


    @RequestMapping("/api/auth/role/delete")
    @ResponseBody
    @SysLog("删除角色信息")
    public ReturnMessage delete(@RequestParam("ids[]") List<String> ids) {
        if (ids.size() <= 0) {
            return ReturnMessageUtil.error(-1, "没有获取到删除的数据");
        }
        for (String data : ids) {
            sysRoleService.removeById(data);
            QueryWrapper<SysRoleMenu> wrapper = new QueryWrapper<>();
            wrapper.eq("role_id", data);
            wrapper.eq("delFlag", false);
            sysRoleMenuService.remove(wrapper);
        }
        return ReturnMessageUtil.sucess();
    }



    @RequestMapping("/api/auth/role/save")
    @ResponseBody
    @SysLog("保存角色信息")
    public ReturnMessage save(@RequestParam("form") String form, @RequestParam("ArrayIds") String ArrayIds, HttpServletRequest request) {
        SysRole sysRole = JSONObject.parseObject(form, SysRole.class);
        List<String> array = JSONObject.parseArray(ArrayIds, String.class);
        String userId = AccountUser.accountUser(request);
        //查询有没有重复的字段
        QueryWrapper<SysRole> wrapper = new QueryWrapper<>();
        wrapper.eq("role_name", sysRole.getRoleName());
        wrapper.eq("delFlag", false);
        int count = sysRoleService.count(wrapper);
        //添加新用户验证loginID是否相同
        if (sysRole.getId() != null && sysRole.getId().length() > 0) {
            //修改
            //原数据库数据
            SysRole old = sysRoleService.getById(sysRole.getId());

            if (!sysRole.getRoleName().equals(old.getRoleName())) {
                if (count != 0) {
                    return ReturnMessageUtil.error(-1, "对不起，你修改的角色重复，请重新创建");
                }
            }
            sysRole.setUpdateId(userId);
            if (sysRoleService.updateById(sysRole)) {
                //删除原来权限
                QueryWrapper<SysRoleMenu> wrapper1 = new QueryWrapper<>();
                wrapper1.eq("role_id", sysRole.getId());
                wrapper1.eq("delFlag", false);
                sysRoleMenuService.remove(wrapper1);
                for (String data : array) {
                    SysRoleMenu sysRoleMenu = new SysRoleMenu();
                    sysRoleMenu.setRoleId(sysRole.getId());
                    sysRoleMenu.setMenuId(data);
                    sysRoleMenu.setCreateId(userId);
                    sysRoleMenuService.save(sysRoleMenu);
                }
                //移除Redis中所有对应此权限的用户
                List<SysUserRole> list = sysUserRoleService.list(new QueryWrapper<SysUserRole>().eq("role_id", sysRole.getId()).eq("delFlag", 0));
                list.stream().forEach((us)->{
                    us.getUserId();
                    redisTemplate.delete(RedisKey.getPermissionKey(us.getUserId()));
                });


                return ReturnMessageUtil.sucess();
            }
            return ReturnMessageUtil.error(-1, "修改数据失败");
        } else {
            //新增
            if (count != 0) {
                return ReturnMessageUtil.error(-1, "对不起，你创建的角色名重复，请重新创建");
            }

            sysRole.setCreateId(userId);
            if (sysRoleService.save(sysRole)) {
                for (String data : array) {
                    SysRoleMenu sysRoleMenu = new SysRoleMenu();
                    sysRoleMenu.setRoleId(sysRole.getId());
                    sysRoleMenu.setMenuId(data);
                    sysRoleMenu.setCreateId(userId);
                    sysRoleMenuService.save(sysRoleMenu);
                }
                return ReturnMessageUtil.sucess();
            }
            return ReturnMessageUtil.error(-1, "新增数据失败");
        }
    }


}