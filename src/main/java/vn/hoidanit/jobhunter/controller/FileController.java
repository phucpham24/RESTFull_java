package vn.hoidanit.jobhunter.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import vn.hoidanit.jobhunter.domain.response.file.ResUploadFileDTO;
import vn.hoidanit.jobhunter.service.FileService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.StorageException;

import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.net.URISyntaxException;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("api/v1")
public class FileController {

    private final FileService fileService;

    @Value("${hoidanit.upload-file.base-uri}")
    private String baseURI;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/files")
    @ApiMessage("upload single file")
    public ResponseEntity<ResUploadFileDTO> upload(@RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam("folder") String folder) throws URISyntaxException, IOException, StorageException {

        // validate
        if (file == null || file.isEmpty()) {
            throw new StorageException("File is not empty, plss upload new file");
        }

        String fileName = file.getOriginalFilename();
        List<String> allowedExtensions = Arrays.asList("pdf", "jpg", "jpeg", "png", "doc", "docx");
        boolean isValid = allowedExtensions.stream().anyMatch(i -> fileName.toLowerCase().endsWith(i));

        if (!isValid) {
            throw new StorageException("File is not valid, only allow " + allowedExtensions.toString());
        }
        // create dir folder
        this.fileService.createDirectory(baseURI + folder);
        // store file
        String uploadFile = this.fileService.store(file, folder);

        ResUploadFileDTO res = new ResUploadFileDTO(uploadFile, Instant.now());

        return ResponseEntity.ok().body(res);
    }

}
