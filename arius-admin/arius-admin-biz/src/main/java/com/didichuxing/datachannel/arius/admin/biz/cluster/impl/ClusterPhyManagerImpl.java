package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.DEFAULT_CLUSTER_HEALTH;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.DEFAULT_CLUSTER_IDC;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.JOIN_MASTER_NODE_MIN_NUMBER;
import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.CLUSTER_PHY;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum.EXCLUSIVE;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum.PRIVATE;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum.PUBLIC;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.CLIENT_NODE;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.DATA_NODE;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.MASTER_NODE;
import static java.util.regex.Pattern.compile;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.biz.page.ClusterPhyPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.mapping.TemplatePhyMappingManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.pipeline.PipelineManager;
import com.didichuxing.datachannel.arius.admin.common.Triple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord.Builder;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterJoinDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterRoleHostDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhyContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterTag;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.setting.ESClusterGetSettingsAllResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterLogicVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterPhyVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.PluginVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.DataCenterEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.RunModeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterConnectionStatus;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterDynamicConfigsEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterDynamicConfigsTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterCreateSourceEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterImportRuleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ResourceLogicLevelEnum;
import com.didichuxing.datachannel.arius.admin.common.event.resource.ClusterPhyEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminTaskException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusScheduleThreadPool;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ClusterUtils;
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ProjectUtils;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterNodeService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpClient;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.service.ProjectService;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchTimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
 *
 * @author ohushenglin_v
 * @date 2022-05-10
 */
@Component
public class ClusterPhyManagerImpl implements ClusterPhyManager {

    private static final ILog                                LOGGER                        = LogFactory
        .getLog(ClusterPhyManagerImpl.class);

    private static final String                              NODE_NOT_EXISTS_TIPS          = "集群缺少类型为%s的节点";

    private static final String                              IP_DUPLICATE_TIPS             = "集群ip:%s重复, 请重新输入";
    /**
     * Map< cluster , Triple< diskUsage , diskTotal , diskUsagePercent >>
     */
    private static final Map<String, Triple<Long, Long, Double>> CLUSTER_NAME_TO_ES_CLUSTER_STATS_TRIPLE_MAP = Maps.newConcurrentMap();
    public static final String SEPARATOR_CHARS = ",";
    public static final String VERSION_PREFIX_PATTERN="^\\d*.\\d*";

    @Autowired
    private ESTemplateService                                esTemplateService;

    @Autowired
    private ClusterPhyService                                clusterPhyService;

    @Autowired
    private ClusterLogicService                              clusterLogicService;

    @Autowired
    private ClusterRoleService clusterRoleService;

    @Autowired
    private ClusterRoleHostService clusterRoleHostService;

    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    private TemplatePhyMappingManager                        templatePhyMappingManager;

    @Autowired
    private PipelineManager templatePipelineManager;

    @Autowired
    private IndexTemplateService indexTemplateService;

    @Autowired
    private TemplatePhyManager                               templatePhyManager;

    @Autowired
    private ClusterRegionService clusterRegionService;

    @Autowired
    private ClusterContextManager                            clusterContextManager;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private OperateRecordService operateRecordService;

    @Autowired
    private ESClusterNodeService                             esClusterNodeService;

    @Autowired
    private ESClusterService                                 esClusterService;

    @Autowired
    private HandleFactory                                    handleFactory;

    @Autowired
    private AriusScheduleThreadPool                          ariusScheduleThreadPool;

    @Autowired
    private ESOpClient                                       esOpClient;

    @Autowired
    private RoleTool roleTool;

    @PostConstruct
    private void init(){
        ariusScheduleThreadPool.submitScheduleAtFixedDelayTask(this::refreshClusterDistInfo,60,180);
    }

    private static final FutureUtil<Void> FUTURE_UTIL = FutureUtil.init("ClusterPhyManagerImpl",20, 40,100);

    @Override
    public boolean copyMapping(String cluster, int retryCount) {
        // 获取物理集群下的所有物理模板
        List<IndexTemplatePhy> physicals = indexTemplatePhyService.getNormalTemplateByCluster(cluster);
        if (CollectionUtils.isEmpty(physicals)) {
            LOGGER.info("class=ESClusterPhyServiceImpl||method=copyMapping||cluster={}||msg=copyMapping no template",
                cluster);
            return true;
        }

        int succeedCount = 0;
        // 遍历物理模板，copy mapping
        for (IndexTemplatePhy physical : physicals) {
            try {
                // 获取物理模板对应的逻辑模板
                IndexTemplate templateLogic = indexTemplateService.getLogicTemplateById(physical.getLogicId());
                // 同步索引的mapping到模板
                Result<MappingConfig> result = templatePhyMappingManager.syncMappingConfig(cluster, physical.getName(),
                    physical.getExpression(), templateLogic.getDateFormat());

                if (result.success()) {
                    succeedCount++;
                    if (!setTemplateSettingSingleType(cluster, physical.getName())) {
                        LOGGER.error(
                            "class=ESClusterPhyServiceImpl||method=copyMapping||errMsg=failedUpdateSingleType||cluster={}||template={}",
                            cluster, physical.getName());
                    }
                } else {
                    LOGGER.warn(
                        "class=ESClusterPhyServiceImpl||method=copyMapping||cluster={}||template={}||msg=copyMapping fail",
                        cluster, physical.getName());
                }
            } catch (Exception e) {
                LOGGER.error("class=ESClusterPhyServiceImpl||method=copyMapping||errMsg={}||cluster={}||template={}",
                    e.getMessage(), cluster, physical.getName(), e);
            }
        }

        return succeedCount * 1.0 / physicals.size() > 0.7;
    }

    @Override
    public void syncTemplateMetaData(String cluster, int retryCount) {
        // 获取物理集群下的所有物理模板
        List<IndexTemplatePhy> physicals = indexTemplatePhyService.getNormalTemplateByCluster(cluster);
        if (CollectionUtils.isEmpty(physicals)) {
            LOGGER.info(
                "class=ESClusterPhyServiceImpl||method=syncTemplateMetaData||cluster={}||msg=syncTemplateMetaData no template",
                cluster);
            return;
        }

        // 遍历物理模板
        for (IndexTemplatePhy physical : physicals) {
            try {
                // 同步模板元数据到ES集群（修改ES集群中的模板）
                templatePhyManager.syncMeta(physical.getId(), retryCount);
                // 同步最新元数据到ES集群pipeline
                templatePipelineManager.syncPipeline(physical,
                    indexTemplateService.getLogicTemplateWithPhysicalsById(physical.getLogicId()));
            } catch (Exception e) {
                LOGGER.error(
                    "class=ESClusterPhyServiceImpl||method=syncTemplateMetaData||errMsg={}||cluster={}||template={}",
                    e.getMessage(), cluster, physical.getName(), e);
            }
        }
    }

    @Override
    public boolean isClusterExists(String clusterName) {
        return clusterPhyService.isClusterExists(clusterName);
    }

    @Override
    public List<ClusterPhyVO> listClusterPhys(ClusterPhyDTO param) {
        List<ClusterPhy> phyClusters = clusterPhyService.listClustersByCondt(param);
        return buildClusterInfo(phyClusters);
    }


    @Override
    public List<ClusterPhyVO> buildClusterInfo(List<ClusterPhy> clusterPhyList) {
        if (CollectionUtils.isEmpty(clusterPhyList)) {
            return Lists.newArrayList();
        }

        List<ClusterPhyVO> clusterPhyVOList = ConvertUtil.list2List(clusterPhyList, ClusterPhyVO.class);

        List<Integer> clusterIds = clusterPhyVOList.stream().map(ClusterPhyVO::getId).collect(Collectors.toList());
        Map<Long, List<ClusterRoleInfo>> roleListMap = clusterRoleService.getAllRoleClusterByClusterIds(clusterIds);
        //3. 设置集群基本统计信息：磁盘使用信息
        long timeForBuildClusterDiskInfo = System.currentTimeMillis();
        for (ClusterPhyVO clusterPhyVO : clusterPhyVOList) {
            FUTURE_UTIL
                    .runnableTask(() -> buildClusterRole(clusterPhyVO, roleListMap.get(clusterPhyVO.getId().longValue())));
        }
        buildClusterPhyWithLogicAndRegion(clusterPhyVOList);
        FUTURE_UTIL.waitExecute();
        LOGGER.info(
            "class=ClusterPhyManagerImpl||method=buildClusterInfo||msg=consumed build cluster belongProjectIds and ProjectName time is {} ms",
            System.currentTimeMillis() - timeForBuildClusterDiskInfo);

        return clusterPhyVOList;
    }

    private long buildClusterPhyWithLogicAndRegion(List<ClusterPhyVO> clusterPhyVOList) {
        List<ClusterRegion> regions = clusterRegionService.listRegionByPhyClusterNames(
            clusterPhyVOList.stream().map(ClusterPhyVO::getCluster).distinct().collect(Collectors.toList()));
        Map<String, Set<Long>> phyCluster2logicClusterIds = Maps.newHashMap();
        Map<Long, ClusterLogicVO> logicClusterId2LogicCluster = Maps.newHashMap();
        Map<Long, ClusterRegionVO> logicClusterId2Region = Maps.newHashMap();
        List<Long> logicIds = Lists.newArrayList();
        regions.stream()
            .filter(region -> StringUtils.isNotBlank(region.getPhyClusterName())
                              && StringUtils.isNotBlank(region.getLogicClusterIds())
                              && !AdminConstant.REGION_NOT_BOUND_LOGIC_CLUSTER_ID.equals(region.getLogicClusterIds()))
            .forEach(region -> {
                String idStr = region.getLogicClusterIds();
                List<Long> list = Lists.newArrayList();
                for (String id : StringUtils.split(idStr, SEPARATOR_CHARS)) {
                    try {
                        list.add(Long.valueOf(id));
                    } catch (NumberFormatException e) {
                        //pass
                    }
                }
                Set<Long> ids = phyCluster2logicClusterIds.getOrDefault(region.getPhyClusterName(), Sets.newHashSet());
                ids.addAll(list);
                phyCluster2logicClusterIds.put(region.getPhyClusterName(), ids);
                list.forEach(id -> {
                    logicClusterId2Region.put(id, ConvertUtil.obj2Obj(region, ClusterRegionVO.class, regionVO -> {
                        regionVO.setClusterName(region.getPhyClusterName());
                    }));
                });
                logicIds.addAll(list);
            });
        if (CollectionUtils.isNotEmpty(logicIds)) {
            List<ClusterLogic> clusterLogicList = clusterLogicService.getClusterLogicListByIds(logicIds);
            logicClusterId2LogicCluster = ConvertUtil.list2Map(clusterLogicList, ClusterLogic::getId,
                clusterLogic -> ConvertUtil.obj2Obj(clusterLogic, ClusterLogicVO.class));
        }

        //3. 设置集群基本统计信息：磁盘使用信息
        long timeForBuildClusterDiskInfo = System.currentTimeMillis();
        for (ClusterPhyVO clusterPhyVO : clusterPhyVOList) {
            Set<Long> set = phyCluster2logicClusterIds.getOrDefault(clusterPhyVO.getCluster(), Sets.newHashSet());
            Map<Long, ClusterLogicVO> finalLogicClusterId2Vo = logicClusterId2LogicCluster;
            set.forEach(id -> {
                clusterPhyVO.addLogicCluster(finalLogicClusterId2Vo.get(id), logicClusterId2Region.get(id));
            });
        }
        return timeForBuildClusterDiskInfo;
    }

    @Override
    public ClusterPhyVO getClusterPhyOverview(Integer clusterId, Integer currentProjectId) {
        // 获取基本信息
        ClusterPhy clusterPhy = clusterPhyService.getClusterById(clusterId);
        if (clusterPhy == null) {
            return new ClusterPhyVO();
        }

        ClusterPhyVO clusterPhyVO = ConvertUtil.obj2Obj(clusterPhy, ClusterPhyVO.class);

        // 构建overView信息
        buildPhyCluster(clusterPhyVO);
        return clusterPhyVO;
    }

    @Override
    public Result<List<String>> listCanBeAssociatedRegionOfClustersPhys(Integer clusterLogicType, Long clusterLogicId) {
        if (!ClusterResourceTypeEnum.isExist(clusterLogicType)) {
            return Result.buildParamIllegal("集群资源类型非法");
        }
        List<String> clusters = Lists.newArrayList();
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(clusterLogicId);
        if (clusterLogic == null) { return Result.buildFail("选定的逻辑集群不存在");}

        ClusterRegion logicClusterRegions = clusterRegionService.getRegionByLogicClusterId(clusterLogic.getId());
        if (null != logicClusterRegions) {
            return Result.buildSucc(clusters);
        }
        return listCanBeAssociatedClustersPhys(clusterLogicType);
    }

    @Override
    public Result<List<String>> listCanBeAssociatedClustersPhys(Integer clusterLogicType) {
        if (!ClusterResourceTypeEnum.isExist(clusterLogicType)) { return Result.buildParamIllegal("集群资源类型非法");}

        List<String> clusters = Lists.newArrayList();
        ClusterPhyDTO clusterPhyDTO = new ClusterPhyDTO();
        clusterPhyDTO.setResourceType(clusterLogicType);
        List<ClusterPhy> list = clusterPhyService.listClustersByCondt(clusterPhyDTO);

        if (PUBLIC.getCode() == clusterLogicType) {
            //共享
            clusters = list.stream().map(ClusterPhy::getCluster).collect(Collectors.toList());
        } else if (EXCLUSIVE.getCode() == clusterLogicType) {
            //独享，需要查询是否有未绑定的region和节点
            clusters = list.stream().filter(cluster -> {
                List<ClusterRegion> regions = clusterRegionService.listPhyClusterRegions(cluster.getCluster());
                if (regions.stream().anyMatch(region -> !clusterRegionService.isRegionBound(region))) {
                    return true;
                }
                List<ClusterRoleHost> roleHostList = clusterRoleHostService
                    .getByRoleAndClusterId(Long.valueOf(cluster.getId()), DATA_NODE.getDesc());
                return roleHostList.stream().anyMatch(node -> node.getRegionId() == -1);
            }).map(ClusterPhy::getCluster).collect(Collectors.toList());
        } else if (PRIVATE.getCode() == clusterLogicType) {
            //独立，未绑定逻辑集群
            clusters = list.stream().filter(cluster -> {
                Set<Long> logicIds = clusterRegionService.getLogicClusterIdByPhyClusterId(cluster.getId());
                return CollectionUtils.isEmpty(logicIds);
            }).map(ClusterPhy::getCluster).collect(Collectors.toList());
        }

        return Result.buildSucc(clusters);
    }
    
    private final Consumer<ESClusterRoleHostDTO> roleClusterHostsTrimHostnameAndPort = esClusterRoleHostDTO -> {
        esClusterRoleHostDTO.setCluster(StringUtils.trim(esClusterRoleHostDTO.getCluster()));
        esClusterRoleHostDTO.setHostname(StringUtils.trim(esClusterRoleHostDTO.getHostname()));
        esClusterRoleHostDTO.setIp(StringUtils.trim(esClusterRoleHostDTO.getIp()));
        esClusterRoleHostDTO.setPort(StringUtils.trim(esClusterRoleHostDTO.getPort()));
        
    };

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<ClusterPhyVO> joinCluster(ClusterJoinDTO param, String operator, Integer projectId) {
        if (param.getProjectId() == null) {
            param.setProjectId(projectId);
        }
         //这里其实是需要一个内置trim 用来保证传输进行的roleClusterHosts是正确的
        param.getRoleClusterHosts().forEach(roleClusterHostsTrimHostnameAndPort);
        Result<Void> checkResult = checkClusterJoin(param, operator);
        if (checkResult.failed()) {
            return Result.buildFail(checkResult.getMessage());
        }
        String esClientHttpAddressesStr = clusterRoleHostService.buildESClientHttpAddressesStr(
                param.getRoleClusterHosts());
        for (ESClusterRoleHostDTO roleClusterHost : param.getRoleClusterHosts()) {
            if (roleClusterHost.getRegionId() == null) {
                roleClusterHost.setRegionId(-1);
            }
        }
        Result<Void> initResult = initClusterJoin(param, esClientHttpAddressesStr);
        if (initResult.failed()) {
            return Result.buildFail(initResult.getMessage());
        }

        try {
          
            // 1.保存物理集群信息(集群、角色、节点)
            Result<ClusterPhyVO> saveClusterResult = saveClusterPhy(param, operator);
          
            if (saveClusterResult.failed()) {
                throw new AdminOperateException(saveClusterResult.getMessage());
            } else {
                SpringTool.publish(new ClusterPhyEvent(param.getCluster(), operator));
                postProcessingForClusterJoin(param);
                operateRecordService.save(new OperateRecord.Builder().project(
                                projectService.getProjectBriefByProjectId(AuthConstant.SUPER_PROJECT_ID))
                        .bizId(saveClusterResult.getData().getId()).content(saveClusterResult.getData().getCluster())
                        .operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_JOIN)
                        .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER).userOperation(operator).build());
            }
           

            return saveClusterResult;
        } catch (AdminOperateException | ElasticsearchTimeoutException e) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=clusterJoin||clusterPhy={}||es operation errMsg={}",
                param.getCluster(), e);
            // 这里必须显示事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.buildFail("接入失败, 请重新尝试接入集群,多次重试不成功,请联系管理员");
        } catch (Exception e) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=clusterJoin||clusterPhy={}||errMsg={}",
                    param.getCluster(), e);
              // 这里必须显示事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.buildFail("操作失败,请联系管理员");
        }
        
    }
    
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteClusterJoin(Integer clusterId, String operator, Integer projectId) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterById(clusterId);
        if (AriusObjUtils.isNull(clusterPhy)) {
            return Result.buildParamIllegal("物理集群不存在");
        }
       
    
        try {
            doDeleteClusterJoin(clusterPhy, operator,projectId);
        } catch (AdminOperateException e) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=deleteClusterJoin||errMsg={}||e={}||clusterId={}",
                e.getMessage(), e, clusterId);
            // 这里显示回滚处理特殊异常场景
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.buildFail(e.getMessage());
        }
        operateRecordService.save(new OperateRecord.Builder()
                        .content(String.format("删除接入集群：%d",clusterId))
                        .operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_JOIN)
                        .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                        .project(projectService.getProjectBriefByProjectId(projectId))
                        .userOperation(operator)
                        .bizId(clusterId)
                .build());
        
        return Result.buildSucc();
    }

    @Override
    public Result<List<PluginVO>> listPlugins(String cluster) {
        return Result.buildSucc(ConvertUtil.list2List(clusterPhyService.listClusterPlugins(cluster), PluginVO.class));
    }

    @Override
    public Result<Map<ClusterDynamicConfigsTypeEnum, Map<String, Object>>> getPhyClusterDynamicConfigs(String cluster) {
        if (!isClusterExists(cluster)) {
           return Result.buildFail(String.format("集群[%s]不存在", cluster));
        }

        ESClusterGetSettingsAllResponse clusterSetting = esClusterService.syncGetClusterSetting(cluster);
        if (null == clusterSetting) {
            return Result.buildFail(String.format("获取集群动态配置信息失败, 请确认是否集群[%s]是否正常", cluster));
        }

        // 构建defaults和persistent的配置信息，transient中的配置信息并非是动态配置的内容
        Map<String, Object> clusterConfigMap = new HashMap<>(16);
        clusterConfigMap.putAll(ConvertUtil.directFlatObject(clusterSetting.getDefaults()));
        clusterConfigMap.putAll(ConvertUtil.directFlatObject(clusterSetting.getPersistentObj()));

        // Map<ClusterDynamicConfigsTypeEnum, Map<String, Object>>中Map的String表示的是动态配置的字段，例如cluster.routing.allocation.awareness.attributes
        // Object则是对应动态配置字段的值
        Map<ClusterDynamicConfigsTypeEnum, Map<String, Object>> clusterDynamicConfigsTypeEnumMapMap = initClusterDynamicConfigs();
        for (ClusterDynamicConfigsEnum param : ClusterDynamicConfigsEnum.valuesWithoutUnknown()) {
            Map<String, Object> dynamicConfig = clusterDynamicConfigsTypeEnumMapMap
                .get(param.getClusterDynamicConfigsType());
            dynamicConfig.put(param.getName(), clusterConfigMap.get(param.getName()));
        }

        return Result.buildSucc(clusterDynamicConfigsTypeEnumMapMap);
    }

    @Override
    public Result<Boolean> updatePhyClusterDynamicConfig(ClusterSettingDTO param, String operator, Integer projectId) {
        final Result<Void> resultCheck = ProjectUtils.checkProjectCorrectly(i -> i, projectId, projectId);
        if (resultCheck.failed()){
            return Result.buildFail(resultCheck.getMessage());
        }
        final Result<Map<ClusterDynamicConfigsTypeEnum, Map<String, Object>>> beforeChangeConfigs =
                getPhyClusterDynamicConfigs(
                param.getClusterName());
        final ClusterPhy clusterByName = clusterPhyService.getClusterByName(param.getClusterName());
        final Result<Boolean> result = clusterPhyService.updatePhyClusterDynamicConfig(param);
        final Result<Map<ClusterDynamicConfigsTypeEnum, Map<String, Object>>> afterChangeConfigs =
                getPhyClusterDynamicConfigs(
                param.getClusterName());
        if (result.success()){
            
            operateRecordService.save(new OperateRecord.Builder()
                            .project(projectService.getProjectBriefByProjectId(AuthConstant.SUPER_PROJECT_ID))
                            .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                            .userOperation(operator)
                            .operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_DYNAMIC_CONF_CHANGE).content(
                            ProjectUtils.getChangeByAfterAndBeforeJson(afterChangeConfigs,beforeChangeConfigs)
        
                    )
                            .bizId(clusterByName.getId())
                    .build());
        }
        return result;
    }

    @Override
    public Result<Set<String>> getRoutingAllocationAwarenessAttributes(String cluster) {
        return Result.buildSucc(clusterPhyService.getRoutingAllocationAwarenessAttributes(cluster));
    }

    @Override
    public List<String> listClusterPhyNameByProjectId(Integer projectId) {
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            //超级projectId返回所有的集群
            List<ClusterPhy> phyList = clusterPhyService.listAllClusters();
            return phyList.stream().map(ClusterPhy::getCluster).distinct().sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        }
        // 非超级管理员，获取拥有的逻辑集群对应的物理集群列表
        List<ClusterLogic> clusterLogicList = clusterLogicService.getOwnedClusterLogicListByProjectId(projectId);
        //项目下的有管理权限逻辑集群会关联多个物理集群
        List<ClusterRegion> regions = clusterRegionService.getClusterRegionsByLogicIds(
            clusterLogicList.stream().map(ClusterLogic::getId).collect(Collectors.toList()));
        return regions.stream().map(ClusterRegion::getPhyClusterName).distinct().sorted(Comparator.naturalOrder())
            .collect(Collectors.toList());
    }

    @Override
    public Result<List<String>> listClusterPhyNameByResourceType(Integer clusterResourceType, Integer projectId) {
        
        if (null != clusterResourceType && !ClusterResourceTypeEnum.isExist(clusterResourceType)) {
            return Result.buildParamIllegal("集群资源类型非法");
        }
        List<String> clusters;
        List<ClusterPhy> clusterPhyList;
        if (null != clusterResourceType) {
            ClusterPhyDTO clusterPhyDTO = new ClusterPhyDTO();
            clusterPhyDTO.setResourceType(clusterResourceType);
            clusterPhyList = clusterPhyService.listClustersByCondt(clusterPhyDTO);

        } else {
            clusterPhyList = clusterPhyService.listAllClusters();
        }
        Set<String> clusterNameSet = ConvertUtil.list2Set(clusterPhyList, ClusterPhy::getCluster);
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            clusters = clusterPhyList.stream().map(ClusterPhy::getCluster).distinct().sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        } else {
            List<ClusterLogic> clusterLogicList = clusterLogicService.getOwnedClusterLogicListByProjectId(projectId);
            //项目下的有管理权限逻辑集群会关联多个物理集群
            List<ClusterRegion> regions = clusterRegionService.getClusterRegionsByLogicIds(
                clusterLogicList.stream().map(ClusterLogic::getId).collect(Collectors.toList()));
            clusters = regions.stream().map(ClusterRegion::getPhyClusterName).distinct()
                .filter(clusterNameSet::contains).sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        }
        return Result.buildSucc(clusters);
    }

    @Override
    public Result<List<String>> getTemplateSameVersionClusterNamesByTemplateId(Integer projectId, Integer templateId) {
        List<String> clusterPhyNameList = listClusterPhyNameByProjectId(projectId);
        // No permission, cut branches and return
        if (CollectionUtils.isEmpty(clusterPhyNameList)) { return Result.buildSucc();}

        IndexTemplateWithPhyTemplates logicTemplateWithPhysicals = indexTemplateService.getLogicTemplateWithPhysicalsById(templateId);
        if (null == logicTemplateWithPhysicals) { return  Result.buildFail(String.format("templateId[%s] is not exist", templateId));}

        IndexTemplatePhy masterPhyTemplate = logicTemplateWithPhysicals.getMasterPhyTemplate();
        if (null == masterPhyTemplate) {
            return  Result.buildFail(String.format("the physicals of templateId[%s] is empty", templateId));
        }

        String cluster = masterPhyTemplate.getCluster();
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(cluster);
        if (null == clusterPhy) { return  Result.buildFail(String.format("the cluster[%s] from templateId[%s] is empty", cluster, templateId));}

        String esVersion = clusterPhy.getEsVersion();

        List<ClusterPhy> clusterPhies = clusterPhyService.listAllClusters();
        
        Predicate<ClusterPhy> matchingSameVersionESVersionPredicate=cp ->
                StringUtils.equals(esVersion,cp.getEsVersion())||
                StringUtils.equals(getESVersionPrefix(esVersion),getESVersionPrefix(cp.getEsVersion()));
        List<String> sameVersionClusterNameList = clusterPhies.stream()
                .filter(Objects::nonNull)
                .filter(r->clusterPhyNameList.contains(r.getCluster()))
                .filter(rCluster ->!StringUtils.equals(logicTemplateWithPhysicals.getMasterPhyTemplate().getCluster(),
                        rCluster.getCluster()))
                .filter(matchingSameVersionESVersionPredicate)
                  .map(ClusterPhy::getCluster)
                .distinct()
                .collect(Collectors.toList());

        return Result.buildSucc(sameVersionClusterNameList);
    }
    
    private  String getESVersionPrefix(String esVersion) {
        Pattern pattern = compile(VERSION_PREFIX_PATTERN);
        final Matcher matcher = pattern.matcher(esVersion);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    @Override
    public List<String> listClusterPhyNodeName(String clusterPhyName) {
        if (null == clusterPhyName) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=getAppClusterPhyNodeNames||errMsg=集群名称为空");
            return Lists.newArrayList();
        }
        return esClusterNodeService.syncGetNodeNames(clusterPhyName);
    }

    @Override
    public List<String> listNodeNameByProjectId(Integer projectId) {
        List<String> appAuthNodeNames = Lists.newCopyOnWriteArrayList();

        List<String> appClusterPhyNames = listClusterPhyNameByProjectId(projectId);
        appClusterPhyNames
            .forEach(clusterPhyName -> appAuthNodeNames.addAll(esClusterNodeService.syncGetNodeNames(clusterPhyName)));

        return appAuthNodeNames;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> deleteCluster(Integer clusterPhyId, String operator, Integer projectId) {
        if (!roleTool.isAdmin(operator)||!AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            return Result.buildFail("当前登录人或项目没有权限进行该操作！");
        }

        ClusterPhy clusterPhy  = clusterPhyService.getClusterById(clusterPhyId);
        if (null == clusterPhy) {
            return Result.buildFail(String.format("物理集群Id[%s]不存在", clusterPhyId));
        }

        Set<Long> clusterLogicIdList = clusterRegionService.getLogicClusterIdByPhyClusterId(clusterPhyId);
        if (CollectionUtils.isNotEmpty(clusterLogicIdList)) {
            List<ClusterLogic> clusterLogicList = clusterLogicService
                .getClusterLogicListByIds(Lists.newArrayList(clusterLogicIdList));
            return Result.buildFail(String.format("物理集群[%s]和逻辑集群[%s]关联", clusterPhy.getCluster(),
                ConvertUtil.list2String(Lists.newArrayList(clusterLogicList), ",", ClusterLogic::getName)));
        }

        List<String> templatePhyNameList = indexTemplatePhyService.getNormalTemplateByCluster(clusterPhy.getCluster())
                .stream().map(IndexTemplatePhy::getName).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(templatePhyNameList)) {
            return Result.buildFail(String.format("物理集群[%s]中已经存在模板[%s]", clusterPhy.getCluster(),
                    ListUtils.strList2String(templatePhyNameList)));
        }

        Result<Boolean> deleteClusterResult = deleteClusterInner(clusterPhyId, projectId);
        if (deleteClusterResult.failed()) {
            return Result.buildFrom(deleteClusterResult);
        }

        SpringTool.publish(new ClusterPhyEvent(clusterPhy.getCluster(), operator));
        operateRecordService.save(new OperateRecord.Builder()
                        .content(clusterPhy.getCluster())
                        .project(projectService.getProjectBriefByProjectId(AuthConstant.SUPER_PROJECT_ID))
                        .operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_OFFLINE)
                        .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                        .userOperation(operator)
                        .bizId(clusterPhyId)
                .build());
        return Result.buildSucc(true);
    }

    @Override
    public Result<Boolean> addCluster(ClusterPhyDTO param, String operator, Integer projectId) {
        Result<Boolean> result = clusterPhyService.createCluster(param, operator);

        if (result.success()) {
            SpringTool.publish(new ClusterPhyEvent(param.getCluster(), operator));
            operateRecordService.save(
                    new OperateRecord.Builder()
                            .operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_NEW)
                            .project(projectService.getProjectBriefByProjectId(projectId))
                            .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                            .content(param.getCluster())
                            .userOperation(operator)
                            .build()
                    
                    
                   );
        }
        return result;
    }

    @Override
    public Result<Boolean> editCluster(ClusterPhyDTO param, String operator) {
        final ClusterPhy oldClusterPhy = clusterPhyService.getClusterById(param.getId());
        final Result<Boolean> result = clusterPhyService.editCluster(param, operator);
        if (result.success()){
    
            if (!StringUtils.equals(oldClusterPhy.getDesc(), param.getDesc())) {
                operateRecordService.save(new Builder().userOperation(operator)
                        .operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_UPGRADE)
                        .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                        .project(projectService.getProjectBriefByProjectId(AuthConstant.SUPER_PROJECT_ID))
                        .bizId(param.getId()).content(String.format("%s,修改集群描述:%s-->%s", oldClusterPhy.getCluster(),
                                oldClusterPhy.getCluster(), param.getCluster()))
                
                        .build());
            }
           
         
        }
        return result;
    }

    @Override
    public PaginationResult<ClusterPhyVO> pageGetClusterPhys(ClusterPhyConditionDTO condition, Integer projectId) throws NotFindSubclassException {
        BaseHandle baseHandle = handleFactory.getByHandlerNamePer(CLUSTER_PHY.getPageSearchType());
        if (baseHandle instanceof ClusterPhyPageSearchHandle) {
            ClusterPhyPageSearchHandle pageSearchHandle = (ClusterPhyPageSearchHandle) baseHandle;
            return pageSearchHandle.doPage(condition, projectId);
        }

        LOGGER.warn("class=ClusterPhyManagerImpl||method=pageGetConsoleClusterVOS||msg=failed to get the ClusterPhyPageSearchHandle");

        return PaginationResult.buildFail("分页获取物理集群信息失败");
    }

    /**
     * 构建用户控制台统计信息: 集群使用率
     */
    @Override
    public void buildPhyClusterStatics(ClusterPhyVO cluster) {
        try {
            Triple<Long, Long, Double> esClusterStaticInfoTriple = getESClusterStaticInfoTriple(cluster.getCluster());
            cluster.setDiskTotal(esClusterStaticInfoTriple.v1());
            cluster.setDiskUsage(esClusterStaticInfoTriple.v2());
            cluster.setDiskUsagePercent(esClusterStaticInfoTriple.v3());
        } catch (Exception e) {
            LOGGER.warn("class=ClusterPhyManagerImpl||method=buildPhyClusterResourceUsage||logicClusterId={}",
                    cluster.getId(), e);
        }
    }

    @Override
    public void buildClusterRole(ClusterPhyVO cluster) {
        try {
            List<ClusterRoleInfo> clusterRoleInfos = clusterRoleService.getAllRoleClusterByClusterId(cluster.getId());

            buildClusterRole(cluster, clusterRoleInfos);
        } catch (Exception e) {
            LOGGER.warn("class=ClusterPhyManagerImpl||method=buildClusterRole||logicClusterId={}", cluster.getId(), e);
        }
    }

    @Override
    public void buildClusterRole(ClusterPhyVO cluster, List<ClusterRoleInfo> clusterRoleInfos) {
        try {
            List<ESClusterRoleVO> roleClusters = ConvertUtil.list2List(clusterRoleInfos, ESClusterRoleVO.class);

            List<Long> roleClusterIds = roleClusters.stream().map(ESClusterRoleVO::getId).collect( Collectors.toList());
            Map<Long, List<ClusterRoleHost>> roleIdsMap = clusterRoleHostService.getByRoleClusterIds(roleClusterIds);

            for (ESClusterRoleVO esClusterRoleVO : roleClusters) {
                List<ClusterRoleHost> clusterRoleHosts = roleIdsMap.get(esClusterRoleVO.getId());
                List<ESClusterRoleHostVO> esClusterRoleHosts = ConvertUtil.list2List(clusterRoleHosts, ESClusterRoleHostVO.class);
                esClusterRoleVO.setEsClusterRoleHostVO(esClusterRoleHosts);
            }

            cluster.setEsClusterRoleVOS(roleClusters);
        } catch (Exception e) {
            LOGGER.warn("class=ClusterPhyManagerImpl||method=buildClusterRole||logicClusterId={}", cluster.getId(), e);
        }
    }

    @Override
    public boolean updateClusterHealth(String clusterPhyName, String operator) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterPhyName);
        if (null == clusterPhy) {
            LOGGER.warn("class=ClusterPhyManagerImpl||method=updateClusterHealth||clusterPhyName={}||msg=clusterPhy is empty", clusterPhyName);
            return false;
        }

        ClusterPhyDTO esClusterDTO      = new ClusterPhyDTO();
        ClusterHealthEnum clusterHealthEnum = esClusterService.syncGetClusterHealthEnum(clusterPhyName);

        esClusterDTO.setId(clusterPhy.getId());
        esClusterDTO.setHealth(clusterHealthEnum.getCode());
        Result<Boolean> editClusterResult = clusterPhyService.editCluster(esClusterDTO, operator);
        if (editClusterResult.failed()) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=updateClusterHealth||clusterPhyName={}||errMsg={}",
                clusterPhyName, editClusterResult.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public boolean updateClusterInfo(String cluster, String operator) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(cluster);
        if (null == clusterPhy) {
            LOGGER.warn("class=ClusterPhyManagerImpl||method=updateClusterInfo||clusterPhyName={}||msg=clusterPhy is empty", cluster);
            return false;
        }

        ESClusterStatsResponse clusterStats = esClusterService.syncGetClusterStats(cluster);
        long totalFsBytes      = clusterStats.getTotalFs().getBytes();
        long usageFsBytes      = clusterStats.getTotalFs().getBytes() - clusterStats.getFreeFs().getBytes();

        double diskFreePercent = clusterStats.getFreeFs().getGbFrac() / clusterStats.getTotalFs().getGbFrac();
        diskFreePercent = CommonUtils.formatDouble(1 - diskFreePercent, 5);

        ClusterPhyDTO esClusterDTO = new ClusterPhyDTO();
        esClusterDTO.setId(clusterPhy.getId());
        esClusterDTO.setDiskTotal(totalFsBytes);
        esClusterDTO.setDiskUsage(usageFsBytes);
        esClusterDTO.setDiskUsagePercent(diskFreePercent);
        Result<Boolean> editClusterResult = clusterPhyService.editCluster(esClusterDTO, operator);
        if (editClusterResult.failed()) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=updateClusterInfo||clusterPhyName={}||errMsg={}",
                    cluster, editClusterResult.getMessage());
            return false;
        }
        
        return true;
    }

    @Override
    public Result<Boolean> checkClusterHealth(String clusterPhyName, String operator) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterPhyName);
        if (null == clusterPhy) {
            return Result.buildFail();
        }

        if (ClusterHealthEnum.GREEN.getCode().equals(clusterPhy.getHealth()) ||
                ClusterHealthEnum.YELLOW.getCode().equals(clusterPhy.getHealth())) {
            return Result.buildSucc(true);
        }

        updateClusterHealth(clusterPhyName, operator);
        return Result.buildSucc();
    }

    @Override
    public Result<Boolean> checkClusterIsExit(String clusterPhyName, String operator) {
        return Result.build(clusterPhyService.isClusterExists(clusterPhyName));
    }

    @Override
    public Result<Boolean> deleteClusterExit(String clusterPhyName, Integer projectId, String operator) {
        if  (!AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            return Result.buildFail("无权限删除集群");
        }

        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterPhyName);
        if (null == clusterPhy) {
            return Result.buildSucc(true);
        }

        return deleteClusterInner(clusterPhy.getId(), projectId);
    }

    @Override
    public Result<List<String>> getPhyClusterNameWithSameEsVersion(Integer clusterLogicType,/*用户在新建逻辑集群阶段已选择的物理集群名称*/String hasSelectedClusterNameWhenBind) {
        //获取可以绑定的物理集群名称列表
        Result<List<String>> canBeAssociatedClustersPhyNamesResult = validLogicAndReturnPhyNamesWhenBindPhy(null, clusterLogicType);
        if (canBeAssociatedClustersPhyNamesResult.failed()) {
            return Result.buildFrom(canBeAssociatedClustersPhyNamesResult);
        }

        //没有指定物理集群名称，则返回全量的匹配数据，不做版本的筛选
        if(AriusObjUtils.isNull(hasSelectedClusterNameWhenBind)) {
            return canBeAssociatedClustersPhyNamesResult;
        }

        //根据已绑定的物理集群的版本进行筛选
        return Result.buildSucc(getPhyClusterNameWithSameEsVersion(hasSelectedClusterNameWhenBind, canBeAssociatedClustersPhyNamesResult.getData()));
    }

    @Override
    public Result<List<String>> getPhyClusterNameWithSameEsVersionAfterBuildLogic(Long clusterLogicId) {
        //获取可以绑定的物理集群名称列表
        Result<List<String>> canBeAssociatedClustersPhyNamesResult = validLogicAndReturnPhyNamesWhenBindPhy(clusterLogicId, null);
        if (canBeAssociatedClustersPhyNamesResult.failed()) {
            return Result.buildFrom(canBeAssociatedClustersPhyNamesResult);
        }

        //获取逻辑集群已绑定的物理集群信息
        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(clusterLogicId);
        if (null == clusterRegion) { return canBeAssociatedClustersPhyNamesResult;}

        //根据已绑定的物理集群的版本进行筛选
        return Result.buildSucc(getPhyClusterNameWithSameEsVersion(clusterRegion.getPhyClusterName(), canBeAssociatedClustersPhyNamesResult.getData()));
    }

    @Override
    public Result<ClusterPhyVO> updateClusterGateway(ClusterPhyDTO param, String operator) {
        ClusterPhyDTO clusterPhyDTO = new ClusterPhyDTO();
        clusterPhyDTO.setId(param.getId());
        clusterPhyDTO.setGatewayUrl(param.getGatewayUrl());
        ClusterPhy oldCluster = clusterPhyService.getClusterById(param.getId());
        Result<Boolean> result = clusterPhyService.editCluster(clusterPhyDTO, operator);
        if (result.failed() ) {
            return Result.buildFail("编辑gateway失败！");
        }
        ClusterPhy clusterPhy = clusterPhyService.getClusterById(param.getId());
        operateRecordService.save(new OperateRecord.Builder()
                        .bizId(param.getId())
                        .userOperation(operator)
                        .project(projectService.getProjectBriefByProjectId(AuthConstant.SUPER_PROJECT_ID))
                        .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                        .operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_GATEWAY_CHANGE)
                        .content(String.format("%s,绑定gateway集群gateway_cluster:%s",oldCluster.getCluster(),param.getGatewayUrl()))
                .build());
        //todo 这里需要记录操作记录hsl
        return Result.buildSucc(ConvertUtil.obj2Obj(clusterPhy, ClusterPhyVO.class));
    }
    @Override
    public List<ClusterRoleInfo> listClusterRolesByClusterId(Integer clusterId){
        return clusterRoleService.getAllRoleClusterByClusterId(clusterId);
    }

    /**************************************** private method ***************************************************/
    
    private Result<Boolean> deleteClusterInner(Integer clusterPhyId, Integer projectId) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterById(clusterPhyId);
        if (null == clusterPhy) {
            return Result.buildFail(String.format("物理集群Id[%s]不存在", clusterPhyId));
        }
        try {
            List<ClusterRoleHost> clusterRoleHosts = clusterRoleHostService.getNodesByCluster(clusterPhy.getCluster());
            // 该物理集群有采集到host数据才执行删除操作
            if (!CollectionUtils.isEmpty(clusterRoleHosts)) {
                Result<Void> deleteHostResult = clusterRoleHostService.deleteByCluster(clusterPhy.getCluster(),
                    projectId);
                if (deleteHostResult.failed()) {
                    throw new AdminOperateException(String.format("删除集群[%s]节点信息失败", clusterPhy.getCluster()));
                }
            }

            Result<Void> deleteRoleResult = clusterRoleService.deleteRoleClusterByClusterId(clusterPhy.getId(),
                projectId);
            if (deleteRoleResult.failed()) {
                throw new AdminOperateException(String.format("删除集群[%s]角色信息失败", clusterPhy.getCluster()));
            }

            List<ClusterRegion> clusterRegionList = clusterRegionService.listPhyClusterRegions(clusterPhy.getCluster());
            if (!AriusObjUtils.isEmptyList(clusterRegionList)) {
                // 该物理集群有Region才删除
                Result<Void> deletePhyClusterRegionResult = clusterRegionService
                    .deleteByClusterPhy(clusterPhy.getCluster());
                if (deletePhyClusterRegionResult.failed()) {
                    throw new AdminOperateException(String.format("删除集群[%s]Region新失败", clusterPhy.getCluster()));
                }
            }

            Result<Boolean> deleteClusterResult = clusterPhyService.deleteClusterById(clusterPhyId, projectId);
            if (deleteClusterResult.failed()) {
                throw new AdminOperateException(String.format("删除集群[%s]信息失败", clusterPhy.getCluster()));
            }
        } catch (AdminOperateException e) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=deleteClusterInfo||clusterName={}||errMsg={}||e={}",
                clusterPhy.getCluster(), e.getMessage(), e);
            // 这里显示回滚处理特殊异常场景
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.buildFail("删除物理集群失败");
        }
        return Result.buildSucc(true);
    }

    /**
     * 更新物理模板setting single_type为true
     * @param cluster  集群
     * @param template 物理模板
     * @return
     */
    private boolean setTemplateSettingSingleType(String cluster, String template) {
        Map<String, String> setting = new HashMap<>(2);
        setting.put(AdminConstant.SINGLE_TYPE_KEY, AdminConstant.DEFAULT_SINGLE_TYPE);
        try {
            return esTemplateService.syncUpsertSetting(cluster, template, setting, 3);
        } catch (ESOperateException e) {
            LOGGER.warn(
                "class=ClusterPhyManagerImpl||method=setTemplateSettingSingleType||errMsg={}||e={}||cluster={}||template={}",
                e.getMessage(), e, cluster, template);
        }

        return false;
    }

    /**
     * 新建逻辑集群和已创建逻辑集群时绑定物理集群时进行校验,并且获取可以绑定的物理集群民称列表
     * @param clusterLogicId 逻辑集群id
     * @param clusterLogicType 逻辑集群类型
     * @return 可以绑定的物理集群民称列表
     */
    Result<List<String>> validLogicAndReturnPhyNamesWhenBindPhy(Long clusterLogicId, Integer clusterLogicType) {
        if (clusterLogicId == null && clusterLogicType == null) {
            return Result.buildFail("传入的参数错误");
        }
        Result<List<String>> canBeAssociatedClustersPhyNames = Result.buildSucc(Lists.newArrayList());
        if (clusterLogicId != null) {
            ClusterLogic clusterLogicById = clusterLogicService.getClusterLogicById(clusterLogicId);
            if (clusterLogicById == null) {
                return Result.buildFail("选定的逻辑集群不存在");
            }
            clusterLogicType = clusterLogicById.getType();
            canBeAssociatedClustersPhyNames = listCanBeAssociatedRegionOfClustersPhys(clusterLogicType, clusterLogicId);
        } else {
            canBeAssociatedClustersPhyNames = listCanBeAssociatedClustersPhys(clusterLogicType);
        }

        if (!ClusterResourceTypeEnum.isExist(clusterLogicType)) {
            return Result.buildParamIllegal("逻辑集群类型非法");
        }

        if (canBeAssociatedClustersPhyNames.failed()) {
            LOGGER.warn("class=ClusterPhyManagerImpl||method=getPhyClusterNameWithSameEsVersionAfterBuildLogic||errMsg={}",
                    canBeAssociatedClustersPhyNames.getMessage());
            Result.buildFail("无法获取对应的物理集群名称列表");
        }

        return canBeAssociatedClustersPhyNames;
    }

    /**
     * 根据已经选定的物理集群筛选出版本相同的可以绑定的物理集群名称列表
     * @param hasSelectedPhyClusterName 已经选择的物理集群名称
     * @param canBeAssociatedClustersPhyNames 可以匹配的物理集群名称列表（待筛选状态）
     * @return 物理集群名称列表
     */
    private List<String> getPhyClusterNameWithSameEsVersion(String hasSelectedPhyClusterName, List<String> canBeAssociatedClustersPhyNames) {
        //获取用户已选择的物理集群的信息
        ClusterPhy hasSelectedCluster = clusterPhyService.getClusterByName(hasSelectedPhyClusterName);
        //如果指定的物理集群名称为null，则返回全量的物理集群名称列表
        if (AriusObjUtils.isNull(hasSelectedPhyClusterName)
                || AriusObjUtils.isNull(hasSelectedCluster)
                || CollectionUtils.isEmpty(canBeAssociatedClustersPhyNames)) {
            return null;
        }

        //筛选出和用户以指定的物理集群的版本号相同的物理集群名称列表
        List<String> canBeAssociatedPhyClusterNameWithSameEsVersion = Lists.newArrayList();
        for (String canBeAssociatedClustersPhyName : canBeAssociatedClustersPhyNames) {
            ClusterPhy canBeAssociatedClustersPhy = clusterPhyService.getClusterByName(canBeAssociatedClustersPhyName);
            if (!AriusObjUtils.isNull(canBeAssociatedClustersPhy)
                    && !AriusObjUtils.isNull(canBeAssociatedClustersPhy.getEsVersion())
                    && !AriusObjUtils.isNull(canBeAssociatedClustersPhy.getCluster())
                    && canBeAssociatedClustersPhy.getEsVersion().equals(hasSelectedCluster.getEsVersion())) {
                canBeAssociatedPhyClusterNameWithSameEsVersion.add(canBeAssociatedClustersPhy.getCluster());
            }
        }

        return canBeAssociatedPhyClusterNameWithSameEsVersion;
    }

    /**
     * 构建物理集群详情
     * @param phyClusters 物理集群元数据信息
     */
    private List<ClusterPhyVO> buildPhyClusters(List<ClusterPhyVO> phyClusters) {

        phyClusters.parallelStream().forEach(this::buildPhyCluster);

        Collections.sort(phyClusters);

        return phyClusters;
    }

    /**
     * 构建物理集群详情
     * @param clusterPhyVO 物理集群元数据信息
     * @return
     */
    private void buildPhyCluster(ClusterPhyVO clusterPhyVO) {
        if (!AriusObjUtils.isNull(clusterPhyVO)) {
            buildPhyClusterStatics(clusterPhyVO);
            buildClusterRole(clusterPhyVO);
            buildClusterPhyWithLogicAndRegion(Collections.singletonList(clusterPhyVO));
        }
    }

    private Result<ClusterPhyVO> saveClusterPhy(ClusterJoinDTO param, String operator) {
        //保存集群信息
        ClusterPhyDTO clusterDTO    =  buildPhyClusters(param, operator);
        Result<Boolean> addClusterRet =  clusterPhyService.createCluster(clusterDTO, operator);
        if (addClusterRet.failed()) { return Result.buildFrom(addClusterRet);}
        return Result.buildSucc(ConvertUtil.obj2Obj(clusterDTO, ClusterPhyVO.class));
    }

    private ClusterPhyDTO buildPhyClusters(ClusterJoinDTO param, String operator) {
        ClusterPhyDTO clusterDTO = ConvertUtil.obj2Obj(param, ClusterPhyDTO.class);

        String clientAddress = clusterRoleHostService.buildESClientHttpAddressesStr(param.getRoleClusterHosts());

        clusterDTO.setDesc(param.getPhyClusterDesc());
        if (StringUtils.isBlank(clusterDTO.getDataCenter())) {
            clusterDTO.setDataCenter(DataCenterEnum.CN.getCode());
        }
        if (null == clusterDTO.getType()) {
            clusterDTO.setType(ESClusterTypeEnum.ES_HOST.getCode());
        }
        clusterDTO.setHttpAddress(clientAddress);
        clusterDTO.setHttpWriteAddress(clientAddress);
        clusterDTO.setIdc(DEFAULT_CLUSTER_IDC);
        clusterDTO.setLevel(ResourceLogicLevelEnum.NORMAL.getCode());
        clusterDTO.setImageName("");
        clusterDTO.setPackageId(-1L);
        clusterDTO.setNsTree("");
        clusterDTO.setPlugIds("");
        clusterDTO.setCreator(operator);
        clusterDTO.setRunMode(RunModeEnum.READ_WRITE_SHARE.getRunMode());
        clusterDTO.setHealth(DEFAULT_CLUSTER_HEALTH);
        return clusterDTO;
    }
    /**
     * 集群接入参数校验
     *
     * @param param    参数
     * @param operator 操作人
     * @return {@link Result}<{@link Void}>
     */
    private Result<Void> checkClusterJoin(ClusterJoinDTO param, String operator) {
        if (AriusObjUtils.isNull(param)) {
            return Result.buildParamIllegal("参数为空");
        }
        ClusterTag clusterTag = ConvertUtil.str2ObjByJson(param.getTags(), ClusterTag.class);

        if (AriusObjUtils.isNull(operator)) {
            return Result.buildParamIllegal("操作人不存在");
        }

        if (!ESClusterTypeEnum.validCode(param.getType())) {
            return Result.buildParamIllegal("非支持的集群类型");
        }

        if (!ClusterResourceTypeEnum.isExist(param.getResourceType())) {
            return Result.buildParamIllegal("非支持的集群资源类型");
        }

        if (ESClusterCreateSourceEnum.ES_IMPORT != ESClusterCreateSourceEnum.valueOf(clusterTag.getCreateSource())) {
            return Result.buildParamIllegal("非集群接入来源");
        }

        if (!ESClusterImportRuleEnum.validCode(param.getImportRule())) {
            return Result.buildParamIllegal("非支持的接入规则");
        }

        return checkClusterNodes(param);
    }

    private Result<Void> checkClusterNodes(ClusterJoinDTO param) {
        List<ESClusterRoleHostDTO> roleClusterHosts = param.getRoleClusterHosts();
        if (CollectionUtils.isEmpty(roleClusterHosts)) {
            return Result.buildParamIllegal("集群节点信息为空");
        }

        // 对于接入集群的节点端口进行校验
        Set<String> wrongPortSet = roleClusterHosts.stream()
                .map(ESClusterRoleHostDTO::getPort)
                .filter(this::wrongPortDetect)
                .collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(wrongPortSet)) {
            return Result.buildParamIllegal("接入集群中端口号存在异常" + wrongPortSet);
        }

        if (ESClusterImportRuleEnum.FULL_IMPORT.getCode() == param.getImportRule()) {
            Set<Integer> roleForNode = roleClusterHosts.stream().map(ESClusterRoleHostDTO::getRole)
                .collect(Collectors.toSet());

            if (!roleForNode.contains(MASTER_NODE.getCode())) {
                return Result.buildParamIllegal(String.format(NODE_NOT_EXISTS_TIPS, MASTER_NODE.getDesc()));
            }

            Map<Integer, List<String>> role2IpsMap = ConvertUtil.list2MapOfList(roleClusterHosts,
                ESClusterRoleHostDTO::getRole, ESClusterRoleHostDTO::getIp);

            List<String> masterIps = role2IpsMap.get(MASTER_NODE.getCode());
            if (masterIps.size() < JOIN_MASTER_NODE_MIN_NUMBER) {
                return Result.buildParamIllegal(String.format("集群%s的masternode角色节点个数要求大于等于1，且不重复", param.getCluster()));
            }

            String duplicateIpForMaster = ClusterUtils.getDuplicateIp(masterIps);
            if (!AriusObjUtils.isBlack(duplicateIpForMaster)) {
                return Result.buildParamIllegal(String.format(IP_DUPLICATE_TIPS, duplicateIpForMaster));
            }

            String duplicateIpForClient = ClusterUtils.getDuplicateIp(role2IpsMap.get(CLIENT_NODE.getCode()));
            if (!AriusObjUtils.isBlack(duplicateIpForClient)) {
                return Result.buildParamIllegal(String.format(IP_DUPLICATE_TIPS, duplicateIpForClient));
            }

            String duplicateIpForData = ClusterUtils.getDuplicateIp(role2IpsMap.get(DATA_NODE.getCode()));
            if (!AriusObjUtils.isBlack(duplicateIpForData)) {
                return Result.buildParamIllegal(String.format(IP_DUPLICATE_TIPS, duplicateIpForData));
            }
        } else {

            List<String> ips = roleClusterHosts.stream().map(ESClusterRoleHostDTO::getIp).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            if (ips.size() < JOIN_MASTER_NODE_MIN_NUMBER) {
                return Result.buildParamIllegal(String.format("集群%s的节点个数要求大于等于1，且不重复", param.getCluster()));
            }

            String duplicateIpForMaster = ClusterUtils.getDuplicateIp(ips);
            if (!AriusObjUtils.isBlack(duplicateIpForMaster)) {
                return Result.buildParamIllegal(String.format(IP_DUPLICATE_TIPS, duplicateIpForMaster));
            }
        }

        if (clusterPhyService.isClusterExists(param.getCluster())) {
            return Result.buildParamIllegal(String.format("物理集群名称:%s已存在", param.getCluster()));
        }

        String esClientHttpAddressesStr = clusterRoleHostService.buildESClientHttpAddressesStr(roleClusterHosts);

        // 密码验证
        Result<Void> passwdResult = checkClusterWithoutPasswd(param, esClientHttpAddressesStr);
        if (passwdResult.failed()) {
            return passwdResult;
        }
        // 同集群验证
        Result<Void> sameClusterResult = checkSameCluster(param.getPassword(), clusterRoleHostService.buildESAllRoleHttpAddressesList(roleClusterHosts));
        if (sameClusterResult.failed()) {
            return Result.buildParamIllegal("禁止同时接入超过两个不同集群节点");
        }

        // 校验 是否接入同一集群
        Result<Void> checkSameClientOrMasterClusterRet = checkSameESClientHttpAddresses(esClientHttpAddressesStr);
        if (checkSameClientOrMasterClusterRet.failed()) {
            return Result.buildFrom(checkSameClientOrMasterClusterRet);
        }

        return Result.buildSucc();
    }

    /**
     * 检查ESClientHttpAddresses是否已经存在
     * @param esClientHttpAddressesStr
     * @return
     */
    private Result<Void> checkSameESClientHttpAddresses(String esClientHttpAddressesStr) {
        List<ClusterPhy> clusterPhies = clusterPhyService.listAllClusters();
        if (CollectionUtils.isEmpty(clusterPhies)) { return Result.buildSucc();}

        // 过滤出目前平台存在的ES集群链接ip:port
        List<String> existClusterHttpAddress = Lists.newArrayList();
        List<String> clusterHttpAddressList = clusterPhies.stream().map(ClusterPhy::getHttpAddress)
                .collect(Collectors.toList());
        for (String clusterHttpAddress : clusterHttpAddressList) {
            for (String httpAddress : ListUtils.string2StrList(clusterHttpAddress)) {
                if (!existClusterHttpAddress.contains(httpAddress.trim())) {
                    existClusterHttpAddress.add(httpAddress.trim());
                }
            }
        }

        List<String> esClientHttpAddressesFromJoin = ListUtils.string2StrList(esClientHttpAddressesStr);
        for (String esClientHttpAddressFromJoin : esClientHttpAddressesFromJoin) {
            if (existClusterHttpAddress.contains(esClientHttpAddressFromJoin.trim())) {
                return Result.buildFail(String.format("平台已经存在相同的集群，连接信息为[%s], 不允许重复接入",
                        esClientHttpAddressFromJoin));
            }
        }

        return Result.buildSucc();
    }

    private Result<Void> initClusterJoin(ClusterJoinDTO param,  String esClientHttpAddressesStr) {
        //获取设置es版本
        Result<Void> esVersionSetResult = initESVersionForClusterJoin(param, esClientHttpAddressesStr);
        if (esVersionSetResult.failed()) {
            return esVersionSetResult;
        }

        return Result.buildSucc();
    }

    /**
     * 检测「未设置密码的集群」接入时是否携带账户信息
     */
    private Result<Void> checkClusterWithoutPasswd(ClusterJoinDTO param, String esClientHttpAddressesStr) {
        ClusterConnectionStatus status = esClusterService.checkClusterPassword(esClientHttpAddressesStr, null);
        if (ClusterConnectionStatus.DISCONNECTED == status) {
            return Result.buildParamIllegal("集群离线未能连通");
        }

        if (!Strings.isNullOrEmpty(param.getPassword())) {
            if (ClusterConnectionStatus.NORMAL == status) {
                return Result.buildParamIllegal("未设置密码的集群，请勿输入账户信息");
            }
            status = esClusterService.checkClusterPassword(esClientHttpAddressesStr, param.getPassword());
            if (ClusterConnectionStatus.UNAUTHORIZED == status) {
                return Result.buildParamIllegal("集群的账户信息错误");
            }
        } else {
            if (ClusterConnectionStatus.UNAUTHORIZED == status) {
                return Result.buildParamIllegal("集群设置有密码，请输入账户信息");
            }
        }
        return Result.buildSucc();
    }

    private Result<Void> checkSameCluster(String passwd, List<String> esClientHttpAddressesList) {
        return esClusterService.checkSameCluster(passwd, esClientHttpAddressesList);
    }

    /**
     * 初始化集群版本
     * @param param
     * @param esClientHttpAddressesStr
     * @return
     */
    private Result<Void> initESVersionForClusterJoin(ClusterJoinDTO param, String esClientHttpAddressesStr) {
        String esVersion = esClusterService.synGetESVersionByHttpAddress(esClientHttpAddressesStr, param.getPassword());
        if (Strings.isNullOrEmpty(esVersion)) {
            return Result.buildParamIllegal(String.format("%s无法获取es版本", esClientHttpAddressesStr));
        }
        param.setEsVersion(esVersion);
        return Result.buildSucc();
    }

    private void doDeleteClusterJoin(ClusterPhy clusterPhy, String operator, Integer projectId) throws AdminOperateException {
        ClusterPhyContext clusterPhyContext = clusterContextManager.getClusterPhyContext(clusterPhy.getCluster());
        if (null == clusterPhyContext) {
            return;
        }

        List<Long> associatedRegionIds = clusterPhyContext.getAssociatedRegionIds();
        for (Long associatedRegionId : associatedRegionIds) {
            Result<Void> unbindRegionResult = clusterRegionService.unbindRegion(associatedRegionId, null, operator,
                    projectId);
            if (unbindRegionResult.failed()) {
                throw new AdminOperateException(String.format("解绑region(%s)失败", associatedRegionId));
            } else {
                //解绑region
                operateRecordService.save(new OperateRecord.Builder().content(
                                String.format("cluster:[%s]解绑region：%d", clusterPhy.getCluster(), associatedRegionId))
                        .operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_REGION_CHANGE)
                        .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                        .project(projectService.getProjectBriefByProjectId(projectId)).userOperation(operator)
                        .bizId(clusterPhy.getCluster()).build());
            }

            Result<Void> deletePhyClusterRegionResult = clusterRegionService.deletePhyClusterRegion(associatedRegionId,
                operator, projectId);
            if (deletePhyClusterRegionResult.failed()) {
                throw new AdminOperateException(String.format("删除region(%s)失败", associatedRegionId));
            } else {
    
                //删除region
                operateRecordService.save(new OperateRecord.Builder().content(
                                String.format("cluster:[%s]删除region：%d", clusterPhy.getCluster(), associatedRegionId))
                        .operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_REGION_CHANGE)
                        .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                        .project(projectService.getProjectBriefByProjectId(projectId)).userOperation(operator)
                        .bizId(clusterPhy.getCluster()).build());
            }
        }
       
        

        List<Long> clusterLogicIds = clusterPhyContext.getAssociatedClusterLogicIds();
        for (Long clusterLogicId : clusterLogicIds) {
            Result<Void> deleteLogicClusterResult = clusterLogicService.deleteClusterLogicById(clusterLogicId,
                operator,projectId);
            if (deleteLogicClusterResult.failed()) {
                throw new AdminOperateException(String.format("删除逻辑集群(%s)失败", clusterLogicId));
            }else {
               //删除逻辑集群
                operateRecordService.save(new OperateRecord.Builder().content(
                                String.format("cluster:[%s]删除逻辑集群：%d", clusterPhy.getCluster(), clusterLogicId))
                        .operationTypeEnum(OperateTypeEnum.MY_CLUSTER_OFFLINE)
                        .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                        .project(projectService.getProjectBriefByProjectId(projectId)).userOperation(operator)
                        .bizId(clusterPhy.getCluster()).build());
            }
        }

        Result<Boolean> deleteClusterResult = clusterPhyService.deleteClusterById(clusterPhy.getId(), projectId);
        if (deleteClusterResult.failed()) {
            throw new AdminOperateException(String.format("删除物理集群(%s)失败", clusterPhy.getCluster()));
        }else {
            //删除物理集群
            operateRecordService.save(
                    new OperateRecord.Builder().content(String.format("cluster:[%s]删除", clusterPhy.getCluster()))
                            .operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_OFFLINE)
                            .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                            .project(projectService.getProjectBriefByProjectId(projectId)).userOperation(operator)
                            .bizId(clusterPhy.getCluster()).build());
        }

        Result<Void> deleteRoleClusterResult = clusterRoleService.deleteRoleClusterByClusterId(clusterPhy.getId(),projectId);
        if (deleteRoleClusterResult.failed()) {
            throw new AdminOperateException(String.format("删除物理集群角色(%s)失败", clusterPhy.getCluster()));
        }else {
            //删除物理集群角色
            operateRecordService.save(
                    new OperateRecord.Builder().content(String.format("cluster:[%s]删除物理集群角色;[%d]",
                                    clusterPhy.getCluster(),clusterPhy.getId()))
                            .operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_OFFLINE)
                            .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                            .project(projectService.getProjectBriefByProjectId(projectId)).userOperation(operator)
                            .bizId(clusterPhy.getCluster()).build());
        }

        Result<Void> deleteRoleClusterHostResult = clusterRoleHostService.deleteByCluster(clusterPhy.getCluster(),projectId);
        if (deleteRoleClusterHostResult.failed()) {
            throw new AdminOperateException(String.format("删除物理集群节点(%s)失败", clusterPhy.getCluster()));
        }else {
            //删除物理集群角色
            operateRecordService.save(
                    new OperateRecord.Builder().content(String.format("cluster:[%s]删除物理集群节点", clusterPhy.getCluster()))
                            .operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_OFFLINE)
                            .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                            .project(projectService.getProjectBriefByProjectId(projectId)).userOperation(operator)
                            .bizId(clusterPhy.getCluster()).build());
        }
    }

    /**
     * 初始化物理集群配置信息
     * @return Map<ClusterDynamicConfigsTypeEnum, Map<String, Object>>中Map的String表示的是动态配置的字段，例如cluster.routing.allocation.awareness.attributes
     * Object则是对应动态配置字段的值
     */
    private Map<ClusterDynamicConfigsTypeEnum, Map<String, Object>> initClusterDynamicConfigs() {
        Map<ClusterDynamicConfigsTypeEnum, Map<String, Object>> esClusterPhyDynamicConfig = Maps.newHashMap();
        for (ClusterDynamicConfigsTypeEnum clusterDynamicConfigsTypeEnum : ClusterDynamicConfigsTypeEnum
            .valuesWithoutUnknown()) {
            esClusterPhyDynamicConfig.put(clusterDynamicConfigsTypeEnum, Maps.newHashMap());
        }

        return esClusterPhyDynamicConfig;
    }

    private Triple<Long/*diskTotal*/, Long/*diskUsage*/, Double/*diskUsagePercent*/> getESClusterStaticInfoTriple(String cluster) {
        Triple<Long, Long, Double> initTriple = buildInitTriple();
        if (!clusterPhyService.isClusterExists(cluster)) {
            LOGGER.error(
                "class=ClusterPhyManagerImpl||method=getESClusterStaticInfoTriple||clusterName={}||msg=cluster is empty",
                cluster);
            return initTriple;
        }

        return getClusterStatsTriple(cluster, initTriple);
    }

    private Triple<Long, Long, Double> getClusterStatsTriple(String cluster, Triple<Long, Long, Double> initTriple) {
        if (CLUSTER_NAME_TO_ES_CLUSTER_STATS_TRIPLE_MAP.containsKey(cluster)) {
            return CLUSTER_NAME_TO_ES_CLUSTER_STATS_TRIPLE_MAP.get(cluster);
        } else {
            refreshClusterStats(cluster, initTriple);
            return initTriple;
        }
    }

    private Triple<Long/*diskTotal*/, Long/*diskTotal*/, Double/*diskUsagePercent*/> buildInitTriple() {
        Triple<Long/*diskTotal*/, Long/*diskTotal*/, Double/*diskUsagePercent*/> triple = new Triple<>();
        triple.setV1(0L);
        triple.setV2(0L);
        triple.setV3(0d);
        return triple;
    }

    private void postProcessingForClusterJoin(ClusterJoinDTO param) throws AdminTaskException {
        esOpClient.connect(param.getCluster());

        if (ESClusterImportRuleEnum.AUTO_IMPORT == ESClusterImportRuleEnum.valueOf(param.getImportRule())) {
            clusterRoleHostService.collectClusterNodeSettings(param.getCluster());
        } else if (ESClusterImportRuleEnum.FULL_IMPORT == ESClusterImportRuleEnum.valueOf(param.getImportRule())) {
            //1.先持久化用户输入的节点信息
            clusterRoleHostService.saveClusterNodeSettings(param);
            //2.直接拉es 更新节点信息，去除因为定时任务触发导致的更新延时
            clusterRoleHostService.collectClusterNodeSettings(param.getCluster());
        }
        updateClusterHealth(param.getCluster(), AriusUser.SYSTEM.getDesc());
      
    }

    private void refreshClusterDistInfo() {
        List<String> clusterNameList = clusterPhyService.listAllClusters().stream().map(ClusterPhy::getCluster)
            .collect(Collectors.toList());
        for (String clusterName : clusterNameList) {
            Triple<Long, Long, Double> initTriple = buildInitTriple();
            refreshClusterStats(clusterName, initTriple);
        }
    }

    private void refreshClusterStats(String clusterName, Triple<Long, Long, Double> initTriple) {
        ESClusterStatsResponse clusterStats = esClusterService.syncGetClusterStats(clusterName);
        if (null != clusterStats && null != clusterStats.getFreeFs() && null != clusterStats.getTotalFs()
                && clusterStats.getTotalFs().getBytes() > 0 && clusterStats.getFreeFs().getBytes() > 0) {
            initTriple.setV1(clusterStats.getTotalFs().getBytes());
            initTriple.setV2(clusterStats.getTotalFs().getBytes() - clusterStats.getFreeFs().getBytes());
            double diskFreePercent = clusterStats.getFreeFs().getGbFrac() / clusterStats.getTotalFs().getGbFrac();
            initTriple.setV3(1 - diskFreePercent);
        }

        CLUSTER_NAME_TO_ES_CLUSTER_STATS_TRIPLE_MAP.put(clusterName, initTriple);
    }

    /**
     * 对于异常的端口号的检测
     * @param port 端口号
     * @return 校验结果
     */
    private boolean wrongPortDetect(String port) {
        try {
            int portValue = Integer.parseInt(port);
            return portValue < AdminConstant.MIN_BIND_PORT_VALUE || portValue > AdminConstant.MAX_BIND_PORT_VALUE;
        } catch (NumberFormatException e) {
            LOGGER.error(
                    "class=ClusterPhyManagerImpl||method=wrongPortDetect||port={}||msg=Integer format error",
                    port);
            return true;
        }
    }
}