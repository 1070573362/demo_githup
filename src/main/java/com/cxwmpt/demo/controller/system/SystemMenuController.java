package com.cxwmpt.demo.controller.system;


import com.nuotadi.base.AccountUser;
import com.nuotadi.common.message.ReturnMessage;
import com.nuotadi.common.message.ReturnMessageUtil;
import com.nuotadi.common.vo.Node;
import com.nuotadi.model.system.SysMenu;
import com.nuotadi.service.api.system.SysMenuService;
import com.nuotadi.system.SysLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;


@Controller
public class SystemMenuController {
    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 菜单栏页面
     *
     * @return
     */

    @RequestMapping("html/menu/page")
    @SysLog("打开菜单管理窗口")
    public String page() {
        return "html/systemSetup/userCenter/menu/Page";
    }

    /**
     * 图标页
     * @return
     */
    @GetMapping("html/menu/icon")
    @SysLog("打开图标窗口")
    public String icon() {
        return "html/systemSetup/userCenter/menu/icon";
    }

    @RequestMapping("/html/menu/addPage")
    public String addPage() {
        return "html/systemSetup/userCenter/menu/Add";
    }

    @RequestMapping("/html/menu/updatePage")
    @SysLog("打开菜单管理的新增或修改窗口")
    public String updatePage(Model model, String id, String action) {
        model.addAttribute("action", action);
        if (StringUtils.isNotBlank(id)) {
            SysMenu menu = sysMenuService.getById(id);
            model.addAttribute("Entity", menu);
        }
        return "html/systemSetup/userCenter/menu/Update";
    }


    /**
     * 获取登录人要显示的菜单
     */
    @RequestMapping("/api/auth/menu/listLoginInfoMenu")
    @ResponseBody
    public   ReturnMessage listLoginInfoMenu(HttpServletRequest request) {
        String userId = AccountUser.accountUser(request);
        List<Node> sysMenuList=sysMenuService.listLoginInfoMenu(userId);
        return  ReturnMessageUtil.sucess(sysMenuList);
    }

    /**
     * dTreeList的数据
     */

    @RequestMapping("/api/auth/menu/getDTreeList")
    @ResponseBody
    @SysLog("查询所有菜单信息")
    public  ReturnMessage getDTreeList() {
        List<Map> list=sysMenuService.getDTreeList();
        return  ReturnMessageUtil.sucess(list,list.size());
    }
//
//
//
    /**
     * 菜单列表treeTable数据
     * orderby
     */
    @PostMapping("/api/auth/menu/listTreeTable")
    @ResponseBody
    @SysLog("查询所有菜单信息")
    public ReturnMessage listTreeTable(HttpServletRequest request) {
        String userId = AccountUser.accountUser(request);

        List<SysMenu> list= sysMenuService.listTreeTable();
        return ReturnMessageUtil.sucess(list,list.size());
    }


    /***
     * 删除自己和子节点
     * @param id
     * @return
     */
    @RequestMapping("/api/auth/menu/delete")
    @ResponseBody
    @SysLog("删除菜单信息")
    public ReturnMessage delete(String id) {
        List<String> sysMentId=sysMenuService.getByIDSelectSubNode(id);
        if(!sysMenuService.removeById(id)){
            return ReturnMessageUtil.error(-1,"删除数据失败");
        }
        for (String data :sysMentId) {
           sysMenuService.removeById(data);
        }
        return ReturnMessageUtil.sucess();
    }
    /**
     * 新增和修改
     * @param sysMenu
     * @return
     */

    @RequestMapping("/api/auth/menu/save")
    @ResponseBody
    @SysLog("保存菜单信息")
    public ReturnMessage save(SysMenu sysMenu, HttpServletRequest request) {

        String userId = AccountUser.accountUser(request);
        if (("").equals(sysMenu.getParentId()) || sysMenu.getParentId() == null) {
            sysMenu.setParentId(0+"");
        }
        //添加新用户验证loginID是否相同
        if (sysMenu.getId() != null&&sysMenu.getId().length()>0) {
            //修改
           if(sysMenu.getId().equals(sysMenu.getParentId())){
               return ReturnMessageUtil.error(-1,"请不要选中自身作为父节点");
           }
            sysMenu.setUpdateId(userId);
            if (sysMenuService.updateById(sysMenu)) {
                return ReturnMessageUtil.sucess();
            }
            return ReturnMessageUtil.error(-1,"修改数据失败");
        } else {
            //新增
            sysMenu.setCreateId(userId);
            if(sysMenuService.save(sysMenu)){
                System.out.println(sysMenu.getId());
                return ReturnMessageUtil.sucess();
            }
            return  ReturnMessageUtil.error(-1,"新增数据失败");
        }
    }



    /**
     * 根据pid查询菜单栏
     * @param parentId
     * @return
     */
    @RequestMapping("/api/auth/menu/listFromPid")
    @ResponseBody
    @SysLog("查询菜单父节点信息")
    public  ReturnMessage listFromPid(@RequestParam("parentId") String parentId) {
        SysMenu sysMenu=sysMenuService.getById(parentId);
         if(sysMenu==null){
             return ReturnMessageUtil.error(-1,"根据pid获取数据为空");
         }
        return ReturnMessageUtil.sucess(sysMenu);
    }
}
