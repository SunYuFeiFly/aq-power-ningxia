package com.wangchen.api;

import com.wangchen.common.ResultLayuiUpload;
import com.wangchen.common.UploadData;
import com.wangchen.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 服务器视频信息
 */

@Slf4j
@Controller
@CrossOrigin(origins = "*")
@RequestMapping("/uploadapi")
public class IndexApiVideoController {

    private final ResourceLoader resourceLoader;

    @Autowired
    private Environment env;

    @Autowired
    public IndexApiVideoController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 上传题目视频(现阶段默认只上传一个视频)
     */
    @PostMapping(value = "/uploadTitleVideo")
    @ResponseBody
    private ResultLayuiUpload uploadTitleVideo(MultipartFile file) {
        try {
            String fileUrl = null;
            // 判断上传视频文件名是否为空
            if (!(null == file.getOriginalFilename() || "".equals(file.getOriginalFilename()))) {
                String imgDir = env.getProperty("tmpDownload");
                // 对文件进行存储处理
                byte[] bytes = file.getBytes();
                String extention = file.getOriginalFilename().substring(file.getOriginalFilename().indexOf("."));
                String fileName = "video_" + System.currentTimeMillis() + extention;
                Path path = Paths.get(imgDir, fileName);
                File fileOld = new File(imgDir, fileName);
                String prefix=fileName.substring(fileName.lastIndexOf("."));
                File file1 = File.createTempFile(prefix, String.valueOf(System.currentTimeMillis())); // 创建临时文件
                FileUtils.copyInputStreamToFile(file.getInputStream(), file1);
                BufferedImage bufferedImage = ImageIO.read(file1); // 通过临时文件获取图片流
                if (bufferedImage == null) {
                    // 证明上传的文件不是视频，获取视频流失败，不进行下面的操作
                }
                if (!fileOld.getParentFile().exists()) {
                    fileOld.getParentFile().mkdirs();
                }

                Files.write(path, bytes);
                fileUrl = env.getProperty("baseUrl") + "uploadapi/upload/" + fileName;
                UploadData data = new UploadData();
                data.setSrc(fileUrl);
                return ResultLayuiUpload.newSuccess(data);
            }
            return ResultLayuiUpload.newFaild("文件不存在");

        } catch (Exception e) {
            log.error("上传题目视频出错,错误信息: {}", e);
            return ResultLayuiUpload.newFaild("上传题目视频失败");
        }
    }


    /**
     * 删除题目视频(单个)
     */
    @PostMapping(value = "/deleteTitleVideo")
    @ResponseBody
    private ResultLayuiUpload deleteTitleVideo(@RequestParam(value = "url",required = false) String url) {
        try {
            // 是否删除成功
            boolean flag = false;
            // 如果删除视频路径为空，直接返回删除失败
            if (url == null && url.length() == 0) {
                return ResultLayuiUpload.newSuccess(flag);
            }
            File file = new File(url);
            // 路径为文件且不为空则进行删除操作
            if (file.isFile() && file.exists()) {
                try {
                    file.delete();
                    flag = true;
                } catch (Exception e) {
                    throw new BusinessException("删除题目视频文件出错");
                }
            }

            return ResultLayuiUpload.newSuccess(flag);
        } catch (Exception e) {
            log.error("删除题目视频文件出错, 错误信息: {}", e);
            return ResultLayuiUpload.newFaild("上传题目视频失败");
        }
    }


    /**
     * 批量删除题目视频(多个)
     *
     * @return
     */
    @PostMapping(value = "/deleteTitleVideos")
    @ResponseBody
    private ResultLayuiUpload deleteTitleVideos(@RequestParam(value = "urls",required = false) String urls) {
        try {
            // 是否删除成功
            boolean flag = false;
            // 如果删除视频路径为空，直接返回删除失败
            if (urls == null && urls.length() == 0) {
                return ResultLayuiUpload.newSuccess(flag);
            }
            if (urls.contains(",")) {
                // 遍历删除
                String[] urlList = urls.split(",");
                for (String s : urlList) {
                    File file = new File(urls);
                    // 路径为文件且不为空则进行删除操作
                    if (file.isFile() && file.exists()) {
                        try {
                            file.delete();
                            flag = true;
                        } catch (Exception e) {
                            throw new BusinessException("批量删除题目视频文件出错");
                        }
                    }
                }
            } else {
                File file = new File(urls);
                // 路径为文件且不为空则进行删除操作
                if (file.isFile() && file.exists()) {
                    try {
                        file.delete();
                        flag = true;
                    } catch (Exception e) {
                        throw new BusinessException("批量删除题目视频文件出错");
                    }
                }
            }

            return ResultLayuiUpload.newSuccess(flag);
        } catch (Exception e) {
            log.error("删除题目视频文件出错, 错误信息: {}", e);
            return ResultLayuiUpload.newFaild("上传题目视频失败");
        }
    }

}
