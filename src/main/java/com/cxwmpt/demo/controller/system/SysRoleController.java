package com.cxwmpt.demo.controller.system;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cxwmpt.demo.annotation.SysLog;
import com.cxwmpt.demo.common.result.ResultCodeEnum;
import com.cxwmpt.demo.common.result.ResultMessage;
import com.cxwmpt.demo.model.system.SysRole;
import com.cxwmpt.demo.model.system.SysRoleMenu;
import com.cxwmpt.demo.model.system.SysUser;
import com.cxwmpt.demo.model.system.SysUserRole;
import com.cxwmpt.demo.service.api.system.SysRoleMenuService;
import com.cxwmpt.demo.service.api.system.SysRoleService;
import com.cxwmpt.demo.service.api.system.SysUserRoleService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

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

import static org.apache.shiro.SecurityUtils.getSubject;

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
    public ResultMessage pageList(@RequestParam Map map) {
        if (map.containsKey("page") && map.containsKey("limit")) {
            PageHelper.startPage(Integer.parseInt(map.get("page").toString()), Integer.parseInt(map.get("limit").toString())).setOrderBy("createDate desc ");
        }
        //判断是否有分页数据传过来
        List<SysRole> list = sysRoleService.AllList(map);
        PageInfo<SysRole> info = new PageInfo<>(list);
        return ResultMessage.success("获取分页数据成功",info.getList(),info.getPageNum(), (int)info.getTotal());


    }
    @RequestMapping("/api/auth/role/AllList")
    @ResponseBody
    @SysLog("查询角色管理的信息")
    public ResultMessage AllList(@RequestParam Map map) {
        //判断是否有分页数据传过来
        List<SysRole> list = sysRoleService.AllList(map);
        return ResultMessage.success(list);
    }


    @RequestMapping("/api/auth/role/delete")
    @ResponseBody
    @SysLog("删除角色信息")
    public ResultMessage delete(@RequestParam("ids[]") List<String> ids) {
        for (String data : ids) {
            sysRoleService.removeById(data);
            QueryWrapper<SysRoleMenu> wrapper = new QueryWrapper<>();
            wrapper.eq("role_id", data);
            wrapper.eq("delFlag", false);
            sysRoleMenuService.remove(wrapper);
        }
        return ResultMessage.success();
    }



    @RequestMapping("/api/auth/role/save")
    @ResponseBody
    @SysLog("保存角色信息")
    public ResultMessage save(@RequestParam("form") String form, @RequestParam("ArrayIds") String ArrayIds, HttpServletRequest request) {
        SysRole sysRole = JSONObject.parseObject(form, SysRole.class);
        List<String> array = JSONObject.parseArray(ArrayIds, String.class);
        //获取登录人信息
        SysUser loginUser = (SysUser) getSubject().getPrincipal();
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
                    return ResultMessage.error(-1, "对不起，你修改的角色重复，请重新创建");
                }
            }
            sysRole.setUpdateId(loginUser.getId());
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
                    sysRoleMenu.setCreateId(loginUser.getId());
                    sysRoleMenuService.save(sysRoleMenu);
                }
                return ResultMessage.success();
            }
            return ResultMessage.error(ResultCodeEnum.UPDATE_DATE_ERROR);
        } else {
            //新增
            if (count != 0) {
                return ResultMessage.error(-1, "对不起，你创建的角色名重复，请重新创建");
            }

            sysRole.setCreateId(loginUser.getId());
            if (sysRoleService.save(sysRole)) {
                for (String data : array) {
                    SysRoleMenu sysRoleMenu = new SysRoleMenu();
                    sysRoleMenu.setRoleId(sysRole.getId());
                    sysRoleMenu.setMenuId(data);
                    sysRoleMenu.setCreateId(loginUser.getId());
                    sysRoleMenuService.save(sysRoleMenu);
                }
                return ResultMessage.success();
            }
            return ResultMessage.error(ResultCodeEnum.ADD_DATE_ERROR);
        }
    }


}
