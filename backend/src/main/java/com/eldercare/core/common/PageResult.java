package com.eldercare.core.common;

import com.github.pagehelper.PageInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应封装（分页约定）
 *
 * @param <T> 列表元素类型
 */
@Data
public class PageResult<T> implements Serializable {

    /** 总记录数 */
    private long total;

    /** 当前页数据 */
    private List<T> list;

    /** 当前页码（从 1 开始） */
    private int pageNum;

    /** 每页大小 */
    private int pageSize;

    /** 总页数 */
    private int pages;

    /**
     * 使用PageHelper分页
     * @param list 必须是 PageHelper 查询出来的集合（内部携带分页信息 total、pageNum 等）
     * @return
     * @param <T>
     */
    public static <T> PageResult<T> of(List<T> list) {
        PageInfo<T> pageInfo = new PageInfo<>(list);
        PageResult<T> result = new PageResult<>();
        result.setTotal(pageInfo.getTotal());
        result.setList(pageInfo.getList());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setPages(pageInfo.getPages());
        return result;
    }

    /**
     * 手动传入分页数据，不依赖任何插件
     * @param list 当前页数据
     * @param total 总记录数
     * @param pageNum 当前页码
     * @param pageSize 每页条数
     * @return
     * @param <T>
     */
    public static <T> PageResult<T> of(List<T> list, long total, int pageNum, int pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setList(list);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setPages(pageSize == 0 ? 0 : (int) ((total + pageSize - 1) / pageSize));
        return result;
    }
}
