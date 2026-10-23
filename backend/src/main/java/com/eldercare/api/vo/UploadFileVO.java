package com.eldercare.api.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 文件上传响应
 * - path: 相对路径（落库用），如 avatar/202608/xxx.jpg
 * - url:  访问 URL（前端预览用），如 /uploads/avatar/202608/xxx.jpg
 */
@Data
@AllArgsConstructor
public class UploadFileVO {

    private String path;

    private String url;
}
