package com.eldercare.admin.service;

import com.eldercare.admin.vo.MemberDetailVO;
import com.eldercare.core.common.PageResult;
import com.eldercare.core.entity.User;

import java.util.List;

/**
 * 管理端会员管理服务
 */
public interface AdminMemberService {

    /**
     * 分页查询会员列表，支持按手机号/姓名/状态/等级筛选
     */
    PageResult<User> page(int pageNum, int pageSize, String phone, String realName, String status, String memberLevel);

    /**
     * 会员详情（基本信息 + 健康记录 + 预约记录）
     */
    MemberDetailVO detail(Long memberId);

    /**
     * 启用/禁用会员
     */
    void updateStatus(Long memberId, String status);

    /**
     * 批量启用/禁用会员，返回实际更新的条数
     */
    int updateStatusBatch(List<Long> memberIds, String status);

    /**
     * 调整会员等级
     */
    void updateLevel(Long memberId, String memberLevel);

    /**
     * 手动调整积分（正数增加/负数扣减）
     */
    void adjustPoints(Long memberId, int points, String reason);

    /**
     * 重置会员密码
     */
    void resetPassword(Long memberId, String newPassword);
}
