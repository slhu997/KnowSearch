package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/2/27 下午2:19
 * @modified By D10865
 *
 * projectid访问索引模板级别次数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel(value = "ProjectIdTemplateAccessCountVO", description = "projectId访问索引模板级别次数")
public class ProjectIdTemplateAccessCountVO {

    @ApiModelProperty("索引模板主键")
    private Integer                                                  templateId;

    @ApiModelProperty("索引逻辑id")
    private Integer                                                  logicTemplateId;

    @ApiModelProperty("索引模板名称")
    private String                                                   templateName;

    @ApiModelProperty("集群名称")
    private String                                                   clusterName;

    @ApiModelProperty("应用账号")
    private Integer                                                  projectId;
    /**
     * 访问索引模板次数，为@accessDetailInfo 访问索引明细的总次数
     */
    @ApiModelProperty("访问索引模板次数")
    private Long                                                     count;
    /**
     *访问索引名称明细数据,key不能是.开头，否则写入es失败
     */
    @ApiModelProperty("访问索引名称明细数据")
    private Map<String/*indexName*/, Long/*access indexName count*/> accessDetailInfo;

    @ApiModelProperty("统计日期")
    private String                                                   date;
}