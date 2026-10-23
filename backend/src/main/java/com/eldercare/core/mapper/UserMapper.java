package com.eldercare.core.mapper;

import com.eldercare.core.entity.User;
import com.eldercare.core.vo.DateCountVO;
import com.eldercare.core.vo.NameValueVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.eldercare.api.dto.UpdateProfileRequest;



import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户表 Mapper
 */
@Mapper
public interface UserMapper {

    User selectById(@Param("id") Long id);

    User selectByPhone(@Param("phone") String phone);

    //用户操作检验用户状态
    User selectSafeById(@Param("id") Long id);

    List<User> selectList();

    // 管理员会员分页筛选
    List<User> selectMemberPage(@Param("phone") String phone,
                                @Param("realName") String realName,
                                @Param("status") String status,
                                @Param("memberLevel") String memberLevel);

    // 按条件查询会员 ID（消息批量推送，无条件时返回全部会员）
    List<Long> selectMemberIdsByCondition(@Param("status") String status,
                                          @Param("memberLevel") String memberLevel,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    // 按条件统计会员数（管理员推送筛选条件）
    long countMemberByCondition(@Param("status") String status,
                                @Param("memberLevel") String memberLevel,
                                @Param("startTime") LocalDateTime startTime,
                                @Param("endTime") LocalDateTime endTime);

    /** 启用/禁用账号 */
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /** 批量启用/禁用账号 */
    int updateStatusBatch(@Param("ids") List<Long> ids, @Param("status") String status);

    /** 调整会员等级 */
    int updateMemberLevel(@Param("id") Long id, @Param("level") String level);

    /** 管理员手动调整积分（同步扣减流水由 Service 记录） */
    int adjustPoints(@Param("id") Long id, @Param("delta") int delta);

    /** 逻辑删除 */
    int logicDelete(@Param("id") Long id);

    /** 修改密码（管理员 用户） */
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    //用户功能
    /** 注册 */
    int insert(User user);

    /** 修改个人信息（个人中心） */
    int updateProfile(@Param("id") Long id,@Param("req") UpdateProfileRequest request);

    //系统功能
    /** 积分原子增减（防并发） */
    int changePoints(@Param("id") Long id, @Param("delta") int delta);

    /** 积分原子扣减（带余额校验，防超扣） */
    int deductPoints(@Param("id") Long id, @Param("delta") int delta);

    /** 仪表盘统计：会员总数 */
    long countMembers();

    /** 仪表盘统计：今日新增会员 */
    long countTodayNewMembers();

    /** 仪表盘：指定日期区间内每日新增会员（按天分组，不包含 end） */
    List<DateCountVO> countNewMembersBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    /** 仪表盘：会员等级分布 */
    List<NameValueVO> countByMemberLevel();
}
