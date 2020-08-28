package com.cxwmpt.demo.controller.base;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nuotadi.common.message.ReturnMessage;
import com.nuotadi.common.message.ReturnMessageUtil;
import com.nuotadi.common.utils.FtpUtil;
import com.nuotadi.model.system.SysDict;
import com.nuotadi.model.system.SysDictIndex;
import com.nuotadi.service.api.system.SysDictIndexService;
import com.nuotadi.service.api.system.SysDictService;
import com.nuotadi.service.impl.base.BaseService;
import com.nuotadi.vo.system.base.DtreeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: backend
 * @description
 * @author: YouName
 * @create: 2020-05-07 15:28
 **/
@Controller
public class BaseController {

    @Autowired
    private BaseService baseService;

    @Autowired
    private SysDictIndexService sysDictIndexService;

    @Autowired
    private SysDictService sysDictService;

    /**
     * 查询单位树
     * @return
     */
    @ResponseBody
    @PostMapping("sys/company/tree")
    public ReturnMessage<Object> showTreeDepts() {

        List<DtreeVO> result = new ArrayList<>();

        result = baseService.showTreeDepts(result, "0");

        return ReturnMessageUtil.sucess(result);
    }

    /**
     * 根据字典类型查询所有字典值
     * @param dictType
     * @return
     */
    @ResponseBody
    @PostMapping("api/auth/sys/dict/selectDictByType")
//    @SysLog("根据字典类型，查询所有字典值")
    public ReturnMessage<Object> selectDictByType(String dictType) {
        //先查询字典索引id
        List<SysDictIndex> indexList = sysDictIndexService.list(new QueryWrapper<SysDictIndex>().eq("dict_type", dictType));

        List<SysDict> list = new ArrayList<>();
        if (indexList != null && indexList.size() > 0) {
            String indexId = indexList.get(0).getId();
            //根据索引ID查询所有的字典值
            list = sysDictService.list(new QueryWrapper<SysDict>().eq("dict_index_id", indexId).eq("delFlag", 0).eq("status", 0));
        }

        return ReturnMessageUtil.sucess(list, list.size());
    }

    /**
     * 系统图片上传
     * @param file
     * @return
     */
    @PostMapping("sys/uploadImg")
    @ResponseBody
    public ReturnMessage<Object> uploadImg(MultipartFile file) {

        String fileName = "";

        try {
            //获取文件名称的后缀信息
            String fileOrginPath = file.getOriginalFilename();
            //拆分文件路径信息
            String[] ext1=fileOrginPath.split("\\.");
            String ext =fileOrginPath.split("\\.")[ext1.length-1];

            fileName = System.currentTimeMillis()+"";
            //ftp上传
            FtpUtil.uploadFile("","",file.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ReturnMessageUtil.sucess(fileName);
    }

}
