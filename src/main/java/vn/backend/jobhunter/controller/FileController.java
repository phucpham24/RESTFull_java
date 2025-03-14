package vn.backend.jobhunter.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import vn.backend.jobhunter.domain.response.file.ResUploadFileDTO;
import vn.backend.jobhunter.service.FileService;
import vn.backend.jobhunter.util.annotation.ApiMessage;
import vn.backend.jobhunter.util.error.StorageException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.net.URISyntaxException;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("api/v1")
public class FileController {

    private final FileService fileService;

    @Value("${ backend.upload-file.base-uri}")
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

    // download file

    @GetMapping("/files")
    @ApiMessage("Download a file")
    public ResponseEntity<Resource> download(
            @RequestParam(name = "fileName", required = false) String fileName,
            @RequestParam(name = "folder", required = false) String folder)
            throws StorageException, URISyntaxException, FileNotFoundException {
        if (fileName == null || folder == null) {
            throw new StorageException("Missing required params : (fileName or folder) in query params.");
        }

        // check file exist (and not a directory)
        long fileLength = this.fileService.getFileLength(fileName, folder);
        if (fileLength == 0) {
            throw new StorageException("File with name = " + fileName + " not found.");
        }

        // download a file
        InputStreamResource resource = this.fileService.getResource(fileName, folder);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentLength(fileLength)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

}
