package com.mntech.shop.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DistributionTeamResponse {

    /** 团队成员用户ID */
    private String userId;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 与我的层级关系 1/2/3 */
    private Integer level;

    /** 绑定时间 */
    private LocalDateTime bindTime;
}