package com.cxwmpt.demo.controller.system;



import com.cxwmpt.demo.annotation.SysLog;
import com.cxwmpt.demo.common.result.ResultCodeEnum;
import com.cxwmpt.demo.common.result.ResultMessage;
import com.cxwmpt.demo.common.vo.Node;
import com.cxwmpt.demo.model.system.SysMenu;
import com.cxwmpt.demo.model.system.SysUser;
import com.cxwmpt.demo.service.api.system.SysMenuService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static org.apache.shiro.SecurityUtils.getSubject;


@Controller
public class SystemMenuController {
    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 菜单栏页面
     *
     * @return
     */

    @RequestMapping("html/system/menu/page")
    @SysLog("打开菜单管理窗口")
    public String page() {
        return "systemSetup/userCenter/menu/Page";
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
    public ResultMessage listLoginInfoMenu() {
        //获取登录人信息
        SysUser loginUser = (SysUser) getSubject().getPrincipal();
        List<Node> sysMenuList=sysMenuService.listLoginInfoMenu(loginUser.getId());
        return  ResultMessage.success(sysMenuList);
    }

    /**
     * dTreeList的数据
     */

    @RequestMapping("/api/auth/menu/getDTreeList")
    @ResponseBody
    @SysLog("查询所有菜单信息")
    public  ResultMessage getDTreeList() {
        List<Map> list=sysMenuService.getDTreeList();
        return  ResultMessage.success("查询所有菜单信息",list);
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
    public ResultMessage listTreeTable() {

        List<SysMenu> list= sysMenuService.listTreeTable();
        return ResultMessage.success("查询所有菜单信息",list);
    }

    /**
     * 根据pid下面的所有子节点(菜单界面使用)
     * orderby
     */
    @PostMapping("/api/auth/menu/getListById")
    @ResponseBody
    public ResultMessage getListByPid(@RequestParam Map map) {
        if (map.containsKey("page") && map.containsKey("limit")) {
            PageHelper.startPage(Integer.parseInt(map.get("page").toString()), Integer.parseInt(map.get("limit").toString()));
        }
        List<SysMenu> list = sysMenuService.getListById(map);
        PageInfo<SysUser> info = new PageInfo(list);
        return ResultMessage.success(info);
    }
    /***
     * 删除自己和子节点
     * @param id
     * @return
     */
    @RequestMapping("/api/auth/menu/delete")
    @ResponseBody
    @SysLog("删除菜单信息")
    public ResultMessage delete(String id) {
        List<String> sysMentId=sysMenuService.getByIDSelectSubNode(id);
        if(!sysMenuService.removeById(id)){
            return ResultMessage.error(ResultCodeEnum.DELETE_DATE_ERROR);
        }
        for (String data :sysMentId) {
           sysMenuService.removeById(data);
        }
        return ResultMessage.success();
    }
    /**
     * 新增和修改
     * @param sysMenu
     * @return
     */

    @RequestMapping("/api/auth/menu/save")
    @ResponseBody
    @SysLog("保存菜单信息")
    public ResultMessage save(SysMenu sysMenu) {
        //获取登录人信息
        SysUser loginUser = (SysUser) getSubject().getPrincipal();
        if (("").equals(sysMenu.getParentId()) || sysMenu.getParentId() == null) {
            sysMenu.setParentId(0+"");
        }
        //添加新用户验证loginID是否相同
        if (sysMenu.getId() != null&&sysMenu.getId().length()>0) {
            //修改
           if(sysMenu.getId().equals(sysMenu.getParentId())){
               return ResultMessage.error(-1,"请不要选中自身作为父节点");
           }
            sysMenu.setUpdateId(loginUser.getId());
            if (sysMenuService.updateById(sysMenu)) {
                return ResultMessage.success();
            }
            return ResultMessage.error(ResultCodeEnum.UPDATE_DATE_ERROR);
        } else {
            //新增
            sysMenu.setCreateId(loginUser.getId());
            if(sysMenuService.save(sysMenu)){
                System.out.println(sysMenu.getId());
                return ResultMessage.success();
            }
            return  ResultMessage.error(ResultCodeEnum.ADD_DATE_ERROR);
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
    public  ResultMessage listFromPid(@RequestParam("parentId") String parentId) {
        SysMenu sysMenu=sysMenuService.getById(parentId);
         if(sysMenu==null){
             return ResultMessage.error(-1,"根据pid获取数据为空");
         }
        return ResultMessage.success(sysMenu);
    }
}
