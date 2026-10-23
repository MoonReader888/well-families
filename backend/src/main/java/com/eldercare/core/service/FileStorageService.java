package com.eldercare.core.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务（体检报告存储）
 */
public interface FileStorageService {

    /**
     * 保存文件
     *
     * @param file     上传文件
     * @param category 分类目录（如 report）
     * @return 相对路径，如 report/202608/abc.pdf，落库用
     */
    String store(MultipartFile file, String category);

    /**
     * 按相对路径加载文件
     */
    Resource load(String relativePath);

    /**
     * 删除文件
     */
    boolean delete(String relativePath);
}
