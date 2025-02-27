package com.didichuxing.datachannel.arius.admin.core.service.template.physic;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplatePhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.IndexTemplatePhyPO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author d06679
 * @date 2019/3/29
 */
public interface IndexTemplatePhyService {
    /**
     * 条件查询
     * @param param 参数
     * @return 物理模板列表
     */
    List<IndexTemplatePhy> getByCondt(IndexTemplatePhyDTO param);

    /**
     * 查询指定逻辑模板对应的物理模板
     * @param logicId 逻辑模板
     * @return list
     */
    List<IndexTemplatePhy> getTemplateByLogicId(Integer logicId);
    IndexTemplatePhy getTemplateByLogicIdAndRole(Integer logicId,Integer role);

    /**
     * 从缓存中查询指定逻辑模板对应的物理模板
     * @param logicId 逻辑模板
     * @return list
     */
    List<IndexTemplatePhy> getValidTemplatesByLogicId(Integer logicId);

    /**
     * 查询指定id的模板
     * @param templatePhyId 物理模板id
     * @return result
     */
    IndexTemplatePhy getTemplateById(Long templatePhyId);

    /**
     * 查询指定id的模板 包含逻辑模板信息
     * @param physicalId 物理模板id
     * @return result
     */
    IndexTemplatePhyWithLogic getTemplateWithLogicById(Long physicalId);

    /**
     * 插入
     * @param param 物理索引模板
     * @return result
     */
    Result<Long> insert(IndexTemplatePhyDTO param) throws AdminOperateException;

    /**
     * 删除
     *
     * @param cluster 物理集群名称
     * @param name    模板名称
     * @return
     */
    Boolean deleteDirtyByClusterAndName(String cluster, String name);

    /**
     * 删除
     * @param physicalId 物理模板id
     * @param operator 操作人
     * @return result
     * @throws ESOperateException e
     */
    Result<Void> delTemplate(Long physicalId, String operator) throws ESOperateException;

    /**
     * 删除
     * @param logicId id
     * @param operator 操作人
     * @return result
     * @throws ESOperateException e
     */
    Result<Void> delTemplateByLogicId(Integer logicId, String operator) throws ESOperateException;

    /**
     * 修改由于逻辑模板修改而物理模板需要同步修改的属性
     * @param param 参数
     * @param operator 操作人
     * @return result
     */
    Result<Void> editTemplateFromLogic(IndexTemplateDTO param, String operator) throws ESOperateException;

    /**
     * 通过集群和名字查询
     * @param cluster 集群
     * @param templateName 名字
     * @return result 不存在返回null
     */
    IndexTemplatePhy getTemplateByClusterAndName(String cluster, String templateName);

    /**
     * 通过集群和名字查询
     * @param cluster 集群
     * @param templateName 名字
     * @return result 不存在返回null
     */
    IndexTemplatePhyWithLogic getTemplateWithLogicByClusterAndName(String cluster, String templateName);

    /**
     * 根据物理模板状态获取模板列表
     * @param cluster 集群
     * @param status 状态 1 常规    -1 删除中     -2 已删除
     * @return list
     */
    List<IndexTemplatePhy> getTemplateByClusterAndStatus(String cluster, int status);

    /**
     * 根据物理模板状态获取模板列表
     * @param logicId 逻辑模板id
     * @param status 状态 1 常规    -1 删除中     -2 已删除
     * @return list
     */
    List<IndexTemplatePhy> getTemplateByLogicIdAndStatus(Integer logicId, Integer status);

    /**
     * 根据物理模板状态获取模板列表
     * @param cluster 集群
     * @return list
     */
    List<IndexTemplatePhy> getNormalTemplateByCluster(String cluster);

    /**
     * 获取模板匹配的索引列表，按着时间排序
     * @param physicalId 物理模板id
     * @return list
     */
    List<String> getMatchNoVersionIndexNames(Long physicalId);

    /**
     * 获取模板匹配的索引列表，按着时间排序
     * @param physicalId 物理模板id
     * @return list
     */
    List<String> getMatchIndexNames(Long physicalId);

    /**
     * 批量获取模板信息
     * @param physicalIds 物理模板id
     * @return list
     */
    List<IndexTemplatePhyWithLogic> getTemplateWithLogicByIds(List<Long> physicalIds);

    /**
     * 全量获取指定物理集群所关联的逻辑模板信息列表
     * @param phyCluster 物理集群名称
     * @return 物理集群下的全量模板信息列表
     */
    List<IndexTemplatePhyWithLogic> getTemplateByPhyCluster(String phyCluster);

    /**
     * 根据名字查询
     * @param template 名字
     * @return list
     */
    List<IndexTemplatePhyWithLogic> getTemplateWithLogicByName(String template);

    /**
     * 获取全量
     * @return list
     */
    List<IndexTemplatePhy> listTemplate();

    List<IndexTemplatePhy> listTemplateWithCache();

    /**
     * 获取IndexTemplatePhysicalWithLogic
     * @return list
     */
    List<IndexTemplatePhyWithLogic> listTemplateWithLogic();

    List<IndexTemplatePhyWithLogic> listTemplateWithLogicWithCache();

    /**
     * 获取集群模板量统计映射
     */
    Map<String, Integer> getClusterTemplateCountMap();

    /**
     * 根绝逻辑模板id列表查询
     * @param logicIds 列表
     * @return list
     */
    List<IndexTemplatePhy> getTemplateByLogicIds(List<Integer> logicIds);

    /**
     * 修改物理模板，名称
     * @param physical 模板
     * @param operator 操作人
     * @return
     */
    Result<Void> updateTemplateName(IndexTemplatePhy physical, String operator) throws ESOperateException;

    /**
     * 修改物理模板，表达
     * @param indexTemplatePhy 模板
     * @param expression 新的表达
     * @param operator 操作人
     * @throws ESOperateException
     * @return result
     */
    Result<Void> updateTemplateExpression(IndexTemplatePhy indexTemplatePhy, String expression,
                                          String operator) throws ESOperateException;

    /**
     * 修改物理模板，表达
     * @param indexTemplatePhy 模板
     * @param shardNum 分片数量
     * @param operator 操作人
     * @throws ESOperateException
     * @return result
     */
    Result<Void> updateTemplateShardNum(IndexTemplatePhy indexTemplatePhy, Integer shardNum,
                                        String operator) throws ESOperateException;

    /**
     * 修改物理模板，角色
     * @param indexTemplatePhy 模板
     * @param templateDeployRoleEnum 角色
     * @param operator 操作人
     * @return
     */
    Result<Void> updateTemplateRole(IndexTemplatePhy indexTemplatePhy, TemplateDeployRoleEnum templateDeployRoleEnum,
                                    String operator);

    /**
     * 更新物理模板
     *
     */
    Result<Void> update(IndexTemplatePhyDTO indexTemplatePhyDTO);

    /**
     *
     * @param physicalPO
     * @return
     */
    IndexTemplatePhyWithLogic buildIndexTemplatePhysicalWithLogic(IndexTemplatePhyPO physicalPO);

    Map<Integer, Integer> getAllLogicTemplatesPhysicalCount();

    /**
     * 校验物理模板信息
     *
     * @param param     参数
     * @param operation 操作
     * @return result
     */
    Result<Void> validateTemplate(IndexTemplatePhyDTO param, OperationEnum operation);

    /**
     * 批量校验物理模板信息
     *
     * @param params    参数
     * @param operation 操作
     * @return result
     */
    Result<Void> validateTemplates(List<IndexTemplatePhyDTO> params, OperationEnum operation);

    /**
     * 获取指定regionId下的所有物理模板列表
     * @param regionId  regionId
     * @return    Result<List<IndexTemplatePhy>>
     */
    Result<List<IndexTemplatePhy>> listByRegionId(Integer regionId);

    IndexTemplatePhyWithLogic buildIndexTemplatePhysicalWithLogicByPhysicalId(Long physicalId);

    List<IndexTemplatePhyPO> getByClusterAndNameAndStatus(String cluster, String name, int code);

    Collection<IndexTemplatePhyPO> getByClusterAndStatus(String cluster, int code);

    boolean updateStatus(Long physicalId, int code);

    boolean updateByIndexTemplatePhyPO(IndexTemplatePhyPO physicalPO);
    
    List<String> getPhyClusterByLogicTemplateId(Integer logicTemplateId);
    
    /**
     * 通过逻辑 id 更新索引模板的分片号
     *
     * @param logicId  逻辑索引 ID，即 index_template_logic 表中索引的 ID。
     * @param shardNum 要更新的分片数
     */
    boolean updateShardNumByLogicId(Integer logicId, Integer shardNum);
}