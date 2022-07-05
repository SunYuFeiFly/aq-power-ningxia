package com.wangchen.api;

import com.wangchen.common.EditorResponse;
import com.wangchen.common.ResultLayuiUpload;
import com.wangchen.common.UploadData;
import com.wangchen.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
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
 * 服务器图片信息
 * @author cheng.zhang
 * @company wangcheng
 * @create 2020/5/16 15:40
 * @Version 1.0
 */

@CrossOrigin(origins = "*")
@Slf4j
@Controller
@RequestMapping("/uploadapi")
public class IndexApiController {

    private final ResourceLoader resourceLoader;
    @Autowired
    private Environment env;

    @Autowired
    public IndexApiController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }


    @RequestMapping(method = RequestMethod.GET, value = "upload/{filename}", produces = "image/png")
    @ResponseBody
    public ResponseEntity<?> getFile(@PathVariable String filename) {
//        String path = "/tmp/upload";
        String path = env.getProperty("tmpDownload");

        log.info("读取图片地址为: path:{}、名称是:{}", path, filename);
        return ResponseEntity.ok(resourceLoader.getResource("file:" + Paths.get(path, filename)));
    }


    /**
     * 读取图片文件(二期 用于对上传视频、图片按时间分类管理)
     */
    @GetMapping(value = "upload/{folderName}/{filename}", produces = "image/png")
    @ResponseBody
    public ResponseEntity<?> getFile01(@PathVariable String folderName,
                                       @PathVariable String filename) {
        String path = env.getProperty("tmpDownload");

        log.info("读取图片地址为: path:{}、名称是:{}", path, filename);
        return ResponseEntity.ok(resourceLoader.getResource("file:" + Paths.get(path +"/"+ folderName, filename)));
    }

    /**
     * 上传商品图片
     * @return
     */
    @PostMapping(value = "/uploadImger")
    @ResponseBody
    private ResultLayuiUpload uploadSingleImger(MultipartFile file) {
        try {
            String fileUrl = null;
            if (!(file.getOriginalFilename() == null || "".equals(file.getOriginalFilename()))) {
                String imgDir = env.getProperty("tmpDownload");
                // 对文件进行存储处理
                byte[] bytes = file.getBytes();
                String extention = file.getOriginalFilename().substring(file.getOriginalFilename().indexOf("."));
                String fileName = "touxiang_" + System.currentTimeMillis() + extention;
                Path path = Paths.get(imgDir, fileName);
                File fileOld = new File(imgDir, fileName);
                // 判断父目录是否存在，不存在：先创建父目录文件夹，再创建指定的文件。
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
            log.error("上传图片商品出错，错误信息: {}", e);
            return ResultLayuiUpload.newFaild("上传商品图片失败");
        }
    }

    /**
     * 上传用户反馈表信息图片
     *
     * @return
     */
    @PostMapping(value = "/uploadFeedbackImger")
    @ResponseBody
    private ResultLayuiUpload uploadFeedbackImger(MultipartFile file) {
        try {
            String fileUrl = null;
            if (!(file.getOriginalFilename() == null || "".equals(file.getOriginalFilename()))) {
                String imgDir = env.getProperty("tmpDownload");
                // 对文件进行存储处理
                byte[] bytes = file.getBytes();
                String extention = file.getOriginalFilename().substring(file.getOriginalFilename().indexOf("."));
                String fileName = "notice_" + System.currentTimeMillis() + extention;
                Path path = Paths.get(imgDir, fileName);
                File fileOld = new File(imgDir, fileName);
                String prefix=fileName.substring(fileName.lastIndexOf("."));
                File file1 = File.createTempFile(prefix, String.valueOf(System.currentTimeMillis())); // 创建临时文件
                FileUtils.copyInputStreamToFile(file.getInputStream(), file1);
                BufferedImage bufferedImage = ImageIO.read(file1); // 通过临时文件获取图片流
                if (bufferedImage == null) {
                    // 证明上传的文件不是图片，获取图片流失败，不进行下面的操作
                }
                Integer width = bufferedImage.getWidth(); // 通过图片流获取图片宽度
                Integer height = bufferedImage.getHeight(); // 通过图片流获取图片高度
                // 判断父目录是否存在，不存在：先创建父目录文件夹，再创建指定的文件。
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
            log.error("上传用户反馈表信息图片出错，错误信息: {}", e);
            return ResultLayuiUpload.newFaild("上传用户反馈表信息图片失败");
        }
    }

    /**
     * 上传公告图片
     */
    @PostMapping(value = "/uploadNoticeImger")
    @ResponseBody
    private ResultLayuiUpload uploadNoticeImger(MultipartFile file) {
        try {
            String fileUrl = null;
            if (!(file.getOriginalFilename() == null || "".equals(file.getOriginalFilename()))) {
                String imgDir = env.getProperty("tmpDownload");
                // 对文件进行存储处理
                byte[] bytes = file.getBytes();
                String extention = file.getOriginalFilename().substring(file.getOriginalFilename().indexOf("."));
                String fileName = "notice_" + System.currentTimeMillis() + extention;
                Path path = Paths.get(imgDir, fileName);
                File fileOld = new File(imgDir, fileName);

                String prefix=fileName.substring(fileName.lastIndexOf("."));
                File file1 = File.createTempFile(prefix, String.valueOf(System.currentTimeMillis())); // 创建临时文件
                FileUtils.copyInputStreamToFile(file.getInputStream(), file1);
                BufferedImage bufferedImage = ImageIO.read(file1); // 通过临时文件获取图片流
                if (bufferedImage == null) {
                    // 证明上传的文件不是图片，获取图片流失败，不进行下面的操作
                }
                // 判断父目录是否存在，不存在：先创建父目录文件夹，再创建指定的文件。
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
            log.error("上传公告图片出错，错误信息: {}", e);
            return ResultLayuiUpload.newFaild("上传公告图片失败");
        }
    }

    @PostMapping(value = "/editor")
    @ResponseBody
    private Object editor(MultipartFile file) {
        try {
            String fileUrl = null;
            if (!(file.getOriginalFilename() == null || "".equals(file.getOriginalFilename()))) {
                String imgDir = env.getProperty("tmpDownload");
                // 对文件进行存储处理
                byte[] bytes = file.getBytes();
                String extention = file.getOriginalFilename().substring(file.getOriginalFilename().indexOf("."));
                String fileName = System.currentTimeMillis() + extention;
                Path path = Paths.get(imgDir, fileName);
                File fileOld = new File(imgDir, fileName);
                // 判断父目录是否存在，不存在：先创建父目录文件夹，再创建指定的文件。
                if (!fileOld.getParentFile().exists()) {
                    fileOld.getParentFile().mkdirs();
                }
                Files.write(path, bytes);
                fileUrl = env.getProperty("baseUrl") + "uploadapi/upload/" + fileName;
                UploadData data = new UploadData();
                data.setSrc(fileUrl);
                return new EditorResponse("0", fileUrl);
            }
            return ResultLayuiUpload.newFaild("文件不存在");
        } catch (Exception e) {
            log.error("上传图片出错，错误信息: {}", e);
            return ResultLayuiUpload.newFaild("上传图片失败");
        }
    }


    private String upload2Server(MultipartFile file) throws Exception {
        try {
            String path = "/tmp/upload";
            File f = new File(path);
            if (!f.exists()) {
                f.mkdirs();
            }
            String fileName = System.currentTimeMillis() + ".png";//+file.getOriginalFilename();
            byte[] bytes = file.getBytes();
            Path p = Paths.get(path + "/" + fileName);
            Files.write(p, bytes);
            return env.getProperty("baseUrl") + "uploadapi/upload/" + fileName;//+ (StringUtils.isEmpty(modules) ? "" : (modules + "/"))
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }


    /**
     * 上传题目图片(现阶段默认只上传一张图片)
     */
    @PostMapping(value = "/uploadTitleImage")
    @ResponseBody
    private ResultLayuiUpload uploadTitleImage(MultipartFile file) {
        try {
            String fileUrl = null;
            if (!(file.getOriginalFilename() == null || "".equals(file.getOriginalFilename()))) {
                String imgDir = env.getProperty("tmpDownload");
                // 对文件进行存储处理
                byte[] bytes = file.getBytes();
                String extention = file.getOriginalFilename().substring(file.getOriginalFilename().indexOf("."));
                String fileName = "title_" + System.currentTimeMillis() + extention;
                Path path = Paths.get(imgDir, fileName);
                File fileOld = new File(imgDir, fileName);
                String prefix=fileName.substring(fileName.lastIndexOf("."));
                File file1 = File.createTempFile(prefix, String.valueOf(System.currentTimeMillis())); // 创建临时文件
                FileUtils.copyInputStreamToFile(file.getInputStream(), file1);
                BufferedImage bufferedImage = ImageIO.read(file1); // 通过临时文件获取图片流
                if (bufferedImage == null) {
                    // 证明上传的文件不是图片，获取图片流失败，不进行下面的操作
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
            log.error("上传题目图片出错，错误信息: {}", e);
            return ResultLayuiUpload.newFaild("上传题目图片失败");
        }
    }


    /**
     * 删除商品图片(单张)
     */
    @PostMapping(value = "/deleteImger")
    @ResponseBody
    private ResultLayuiUpload deleteSingleImger(@RequestParam(value = "url") String url) {
        try {
            // 判断删除路径是否为空
            if (url == null && url.length() == 0) {
                return ResultLayuiUpload.newFaild("商品图片路径为空，删除失败！");
            } else {
                // 公共删除图片方法
                Boolean isDelete = deleteImage(url);
                return ResultLayuiUpload.newSuccess(isDelete);
            }
        } catch (Exception e) {
            log.error("删除商品图片出错，错误信息: {}", e);
            return ResultLayuiUpload.newFaild("删除商品图片失败");
        }
    }


    /**
     * 删除反馈表图片(单张)
     */
    @PostMapping("/deleteFeedbackImger")
    @ResponseBody
    public ResultLayuiUpload deleteFeedbackImger(@RequestParam(value = "url") String url) {
        try {
            // 判断删除路径是否为空
            if (url == null && url.length() == 0) {
                return ResultLayuiUpload.newFaild("反馈表图片路径为空，删除失败！");
            } else {
                // 公共删除图片方法
                Boolean isDelete = deleteImage(url);
                return ResultLayuiUpload.newSuccess(isDelete);
            }
        } catch (Exception e) {
            log.error("删除反馈表图片出错，错误信息: {}", e);
            return ResultLayuiUpload.newFaild("删除反馈表图片失败");
        }
    }


    /**
     * 删除公告图片(单张)
     */
    @PostMapping("/deleteNoticeImger")
    @ResponseBody
    public ResultLayuiUpload deleteNoticeImger(@RequestParam(value = "url") String url) {
        try {
            // 判断删除路径是否为空
            if (url == null && url.length() == 0) {
                return ResultLayuiUpload.newFaild("公告图片路径为空，删除失败！");
            } else {
                // 公共删除图片方法
                Boolean isDelete = deleteImage(url);
                return ResultLayuiUpload.newSuccess(isDelete);
            }
        } catch (Exception e) {
            log.error("删除公告图片出错，错误信息: {}", e);
            return ResultLayuiUpload.newFaild("删除公告图片失败");
        }
    }


    /**
     * 删除题目图片(单张)
     */
    @PostMapping("/deleteTitleImage")
    @ResponseBody
    public ResultLayuiUpload deleteTitleImage(@RequestParam(value = "url") String url) {
        try {
            // 判断删除路径是否为空
            if (url == null && url.length() == 0) {
                return ResultLayuiUpload.newFaild("题目图片路径为空，删除失败！");
            } else {
                // 公共删除图片方法
                Boolean isDelete = deleteImage(url);
                return ResultLayuiUpload.newSuccess(isDelete);
            }
        } catch (Exception e) {
            log.error("删除题目图片出错，错误信息: {}", e);
            return ResultLayuiUpload.newFaild("删除题目图片失败");
        }
    }


    /**
     * 公共删除图片方法
     */
    private Boolean deleteImage(String url) {
        // 是否删除成功
        boolean flag = false;
        File file = new File(url);
        // 路径为文件且不为空则进行删除操作
        if (file.isFile() && file.exists()) {
            try {
                file.delete();
                flag = true;
            } catch (Exception e) {
                throw new BusinessException("删除图片（公共方法）出错！");
            }
        }
        return flag;
    }

}
