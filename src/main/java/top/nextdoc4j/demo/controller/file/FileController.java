package top.nextdoc4j.demo.controller.file;

import cn.hutool.core.util.IdUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.nextdoc4j.demo.model.base.R;
import top.nextdoc4j.demo.model.resp.FileUploadResp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件控制器
 *
 * @author echo
 * @date 2025/09/17
 */
@Tag(name = "文件上传管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/file")
public class FileController {

    @Operation(summary = "单文件上传（带 consumes）", description = "上传单个文件，明确使用 multipart/form-data")
    @PostMapping(value = "/single-with-multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Parameter(name = "file", description = "文件")
    public R<FileUploadResp> uploadSingleWithMultipart(@RequestPart("file") MultipartFile file) {
        return R.ok(buildFileUploadResp(file, "single"));
    }

    @Operation(summary = "单文件上传（不带 consumes）", description = "上传单个文件，不指定 consumes，让 Spring 自动推断")
    @PostMapping("/single")
    @Parameter(name = "file", description = "文件")
    public R<FileUploadResp> uploadSingle(@RequestPart("file") MultipartFile file) {
        return R.ok(buildFileUploadResp(file, "single"));
    }


    @Operation(summary = "批量文件上传（带 consumes）", description = "批量上传文件，明确使用 multipart/form-data")
    @PostMapping(value = "/batch-with-multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Parameter(name = "files", description = "文件列表")
    public R<List<FileUploadResp>> uploadBatchWithMultipart(@RequestParam("files") List<MultipartFile> files) {
        return R.ok(files.stream().map(file -> buildFileUploadResp(file, "files")).toList());
    }

    @Operation(summary = "批量文件上传（不带 consumes）", description = "批量上传文件，不指定 consumes")
    @PostMapping("/batch")
    @Parameter(name = "files", description = "文件列表")
    public R<List<FileUploadResp>> uploadBatch(@RequestParam("files") List<MultipartFile> files) {
        return R.ok(files.stream().map(file -> buildFileUploadResp(file, "files")).toList());
    }

    private FileUploadResp buildFileUploadResp(MultipartFile file, String type) {
        FileUploadResp resp = new FileUploadResp();
        resp.setOriginalName(file.getOriginalFilename());
        resp.setFileName(IdUtil.fastSimpleUUID() + "_" + file.getOriginalFilename());
        resp.setFileSize(file.getSize());
        resp.setContentType(file.getContentType());
        resp.setUrl("https://demo.top/" + type + "/" + resp.getFileName());
        resp.setUploadTime(LocalDateTime.now());
        return resp;
    }
}
