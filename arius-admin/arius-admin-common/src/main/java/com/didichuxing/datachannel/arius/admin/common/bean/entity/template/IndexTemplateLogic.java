package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import com.didichuxing.datachannel.arius.admin.common.constant.template.DataTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.StringResponsible;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chengxiang
 * @date 2022/5/11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexTemplateLogic extends BaseEntity implements StringResponsible, Comparable<IndexTemplateLogic> {

    private Integer id;

    /**
     * 索引模板名称
     */
    private String  name;

    /**
     * appid
     */
    private Integer appId;

    /**
     * 用户数据类型
     * @see DataTypeEnum
     */
    private Integer dataType;

    /**
     * 索引滚动格式
     */
    private String  dateFormat;

    /**
     * 数据中心
     */
    private String  dataCenter;

    /**
     * 数据保存时长 单位天
     */
    private Integer expireTime;

    /**
     * 热数据保存时长 单位天
     */
    private Integer hotTime;

    /**
     * 副本保存时长 单位天
     */
    private Integer replicaTime;

    /**
     * 成本部门
     */
    private String  libraDepartmentId;

    /**
     * 成本部门
     */
    private String  libraDepartment;

    /**
     * 责任人
     */
    private String  responsible;

    /**
     * 时间字段
     */
    private String  dateField;

    /**
     * 时间字段的格式
     */
    private String  dateFieldFormat;

    /**
     * id字段
     */
    private String  idField;

    /**
     * routing字段
     */
    private String  routingField;

    /**
     * 表达式
     */
    private String  expression;

    /**
     * 逻辑集群id
     */
    private Long resourceId;

    /**
     * 备注
     */
    private String  desc;

    /**
     * 规格 单位台 每台的资源量就是DOCKER类型的规格的资源；与物理部署的模板无关
     */
    private Double  quota;

    /**
     * 写入限流值，
     * writeRateLimit = 0 禁止写入，
     * writeRateLimit = -1 不限流，
     * writeRateLimit = 123 具体的写入tps限流值，即单台client每秒写入123条文档
     */
    private Integer writeRateLimit;

    /**
     * pipeline
     */
    private String  ingestPipeline;

    /**
     * 是否禁止读
     */
    private Boolean blockRead;

    /**
     * 是否禁止写
     */
    private Boolean blockWrite;

    /**
     * 服务等级
     */
    private Integer level;

    private Boolean hasDCDR;

    private Long    checkPointDiff;

    private String  openSrv;

    @Override
    public int compareTo(IndexTemplateLogic o) {
        if (null == o) {
            return 0;
        }

        return o.getId().intValue() - this.getId().intValue();
    }
}
