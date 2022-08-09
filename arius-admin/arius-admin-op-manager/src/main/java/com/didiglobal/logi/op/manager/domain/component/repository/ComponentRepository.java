package com.didiglobal.logi.op.manager.domain.component.repository;

import com.didiglobal.logi.op.manager.domain.component.entity.Component;

/**
 * @author didi
 * @date 2022-07-12 11:08 上午
 */
public interface ComponentRepository {
    /**
     * 保存组件
     * @param component
     * @return
     */
    int saveComponent(Component component);

    /**
     * 通过id获取组件
     * @param componentId
     * @return
     */
    Component getComponentById(int componentId);


    /**
     * 更新包含的组件信息
     * @param componentId
     * @param containIds
     */
    void updateContainIds(int componentId, String containIds);


    /**
     * 更新组件信息
     * @param component
     */
    void updateComponent(Component component);
}
