package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.operaterecord;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.operaterecord.OperateRecordVO;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.google.common.collect.Lists;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author d06679
 * @date 2017/10/9
 */
@RestController
@RequestMapping(V2_OP + "/record")
@Api(value = "运维操作记录接口(REST)")
public class OperateRecordController {

    private static final int     MAX_RECORD_COUNT = 200;

    @Autowired
    private OperateRecordService operateRecordService;

    @PostMapping("/list")
    @ResponseBody
    @ApiOperation(value = "查询操作记录接口", notes = "")
    public Result<List<OperateRecordVO>> list(@RequestBody OperateRecordDTO query) {
        List<OperateRecordVO> records = ConvertUtil.list2List(operateRecordService.list(query), OperateRecordVO.class);

        if (records.size() > MAX_RECORD_COUNT) {
            records = records.subList(0, MAX_RECORD_COUNT);
        }

        fillVOField(records);
        return Result.buildSucc(records);
    }

    @GetMapping("/listModules")
    @ResponseBody
    @ApiOperation(value = "获取所有模块", notes = "")
    public Result<List<Map<String, Object>>> listModules() {
        List<Map<String, Object>> objects = Lists.newArrayList();
        for (ModuleEnum moduleEnum : ModuleEnum.values()) {
            objects.add(moduleEnum.toMap());
        }
        return Result.buildSucc(objects);
    }

    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value = "保存操作记录接口", notes = "")
    public Result add(@RequestBody OperateRecordDTO param) {
        return operateRecordService.save(param);
    }

    /**************************************** private method ****************************************************/
    private void fillVOField(List<OperateRecordVO> records) {
        if(CollectionUtils.isEmpty(records)){return;}

        for(OperateRecordVO vo : records){
            vo.setModule(ModuleEnum.valueOf(vo.getModuleId()).getDesc());
            vo.setOperate(OperationEnum.valueOf(vo.getOperateId()).getDesc());
        }
    }

    // *************************************** RESTFUL  API ***************************************

    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "查询操作记录接口", notes = "")
    public Result<List<OperateRecordVO>> getOperationRecords(@RequestParam OperateRecordDTO query) {
        List<OperateRecordVO> records = ConvertUtil.list2List(operateRecordService.list(query), OperateRecordVO.class);

        if (records.size() > MAX_RECORD_COUNT) {
            records = records.subList(0, MAX_RECORD_COUNT);
        }

        fillVOField(records);
        return Result.buildSucc(records);
    }
}
