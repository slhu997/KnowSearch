package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.logic;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterRegionManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangshu
 * @date 2020/10/10
 */
@RestController
@RequestMapping(V3 + "/cluster/logic/region")
@Api(tags = "ES逻辑集群region接口(REST)")
public class ESLogicClusterRegionController {


    @Autowired
    private ClusterRegionManager clusterRegionManager;

    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "查询逻辑集群region列表接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "logicClusterId", value = "logicClusterId", required = true) })
    public Result<List<ClusterRegionVO>> listLogicClusterRegions(@RequestParam("logicClusterId") Long logicClusterId) {
    
        return clusterRegionManager.buildLogicClusterRegionVOByLogicClusterId(logicClusterId);
    }
}