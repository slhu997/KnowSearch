package com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm;


import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESClusterRoleInfoPO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 集群角色DAO
 * @author chengxiang
 * @date 2022/5/9
 */
@Repository
public interface ESClusterRoleInfoDAO {

    /**
     * 插入一条记录
     * @param param
     * @return
     */
    int insert(ESClusterRoleInfoPO param);

    /**
     * 根据id 获取集群角色信息
     * @param id
     * @return
     */
    ESClusterRoleInfoPO getById(Long id);

    /**
     * 根据clusterId 和 roleClusterName 查询详情
     * @param clusterId
     * @param roleClusterName
     * @return
     */
    ESClusterRoleInfoPO getByClusterIdAndClusterRole(@Param("clusterId") Long clusterId,
                                                     @Param("roleClusterName")String roleClusterName);

    /**
     * 根据clusterId 和 role 查询详情
     * @param clusterId
     * @return
     */
    List<ESClusterRoleInfoPO> listByClusterId(String clusterId);

    /**
     * 根据clusterId 查询详情
     * @param clusterIds
     * @return
     */
    List<ESClusterRoleInfoPO> listByClusterIds(List<String> clusterIds);

    /**
     * 根据clusterId 和 role 修改podCount
     * @param param
     * @return
     */
    int update(ESClusterRoleInfoPO param);

    /**
     * 根据clusterId 和 role 修改es版本
     * @param elasticClusterId
     * @param role
     * @param esVersion
     * @return
     */
    int updateVersionByClusterIdAndRole(@Param("elasticClusterId") Long elasticClusterId,
                                        @Param("role")String role,
                                        @Param("esVersion")String esVersion);

    /**
     * 根据clusterId 和 role 查询详情
     * @param clusterId
     * @param role
     * @return
     */
    ESClusterRoleInfoPO getByClusterIdAndRole(@Param("clusterId") Long clusterId,
                                              @Param("role")String role);

    /**
     * 根据clusterId 删除记录
     * @param clusterId
     * @return
     */
    int delete(Integer clusterId);

    /**
     * 根据clusterId 和 role 删除记录
     * @param clusterId
     * @param role
     * @return
     */
    int deleteRoleClusterByCluterIdAndRole(@Param("clusterId") Long clusterId,
                                           @Param("role")String role);

}
