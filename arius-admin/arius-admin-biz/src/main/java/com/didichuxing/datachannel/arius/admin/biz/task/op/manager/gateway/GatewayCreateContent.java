package com.didichuxing.datachannel.arius.admin.biz.task.op.manager.gateway;

import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneraInstallComponentDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 该类用于创建集群
 *
 * @author shizeying
 * @date 2022/10/20
 * @since 0.3.2
 */

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class GatewayCreateContent extends GeneraInstallComponentDTO {
    private String memo;
    private String dataCenter;
    private String proxyAddress;
    private String clusterType;
}