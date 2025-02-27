package com.didichuxing.datachannel.arius.admin.common.bean.vo.project;

import com.didiglobal.knowframework.security.common.vo.user.UserBriefVO;
import com.didiglobal.knowframework.security.common.vo.user.UserVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "用户信息")
public class UserExtendVO extends UserVO {
    @ApiModelProperty(value = "持有管理员角色的项目成员", dataType = "List<UserBriefVO>", required = false)
    private List<UserBriefVO> userListWithAdminRole;

    @ApiModelProperty(value = "作为责任人的应用列表", dataType = "List<String>", required = false)
    private List<String> ownProjects;

    @ApiModelProperty(value = "作为唯一责任人的应用列表", dataType = "List<String>", required = false)
    private List<String> singleOwnerOfProjects;
}