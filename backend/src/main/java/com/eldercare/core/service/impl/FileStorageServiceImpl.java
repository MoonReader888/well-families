package com.eldercare.core.service.impl;

import com.eldercare.core.exception.BusinessException;
import com.eldercare.core.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 本地磁盘文件存储实现
 * 目录结构：{upload-dir}/{category}/{yyyyMM}/{uuid}.{ext}
 */
@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path uploadDir;

    public FileStorageServiceImpl(@Value("${eldercare.file.upload-dir}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    /**
     * 保存文件
     */
    @Override
    public String store(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        /**
         * 生成文件名
         */
        String originalFilename = StringUtils.hasText(file.getOriginalFilename())
                ? file.getOriginalFilename()
                : "unnamed";
        String ext = getExtension(originalFilename);
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        String yyyyMM = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));

        /**
         * 保存文件到指定目录
         */
        Path dir = uploadDir.resolve(category).resolve(yyyyMM);
        try {
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(filename).toFile());
        } catch (IOException e) {
            log.error("文件保存失败: category={}, name={}", category, originalFilename, e);
            throw new BusinessException("文件保存失败，请稍后再试");
        }
        return category + "/" + yyyyMM + "/" + filename;
    }
    /**
     * 加载文件
     */
    @Override
    public Resource load(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            throw new BusinessException("文件路径不能为空");
        }
        Path path = uploadDir.resolve(relativePath).normalize();
        // 防路径穿越
        if (!path.startsWith(uploadDir)) {
            throw new BusinessException("非法的文件路径");
        }
        if (!Files.exists(path)) {
            throw new BusinessException("文件不存在");
        }
        return new FileSystemResource(path);
    }

    @Override
    public boolean delete(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return false;
        }
        Path path = uploadDir.resolve(relativePath).normalize();
        if (!path.startsWith(uploadDir) || !Files.exists(path)) {
            return false;
        }
        try {
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("文件删除失败: {}", relativePath, e);
            return false;
        }
    }
    /**
     * 获取文件扩展名
     */
    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "bin";
        }
        return filename.substring(dot + 1).toLowerCase();
    }
}
