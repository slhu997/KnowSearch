package com.didiglobal.logi.op.manager.infrastructure.db.mapper;

import com.didiglobal.logi.op.manager.infrastructure.db.ComponentHostPO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-19 3:37 下午
 */
@Repository
public interface ComponentHostDao {
     /**
      * 插入数据
      * @param hostPO
      */
     void insert(ComponentHostPO hostPO);

     /**
      * 更新状态
      * @param componentId
      * @param host
      * @param groupId
      * @param status
      */
     void updateStatus(int componentId, String host, int groupId, int status);

     /**
      * 获取所有组件host列表
      * @return List<ComponentHostPO> 组件po
      */
     List<ComponentHostPO> listAll();
}
