package com.goomoong.room9backend.service.file;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
//import com.amazonaws.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import com.goomoong.room9backend.domain.file.dto.fileDto;
import com.goomoong.room9backend.exception.ImageTypeException;
import com.goomoong.room9backend.exception.S3FileUploadException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class S3Uploader {

//        private final AmazonS3Client amazonS3Client;
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public List<fileDto> uploadFileList(String dirName, List<MultipartFile> files){
        return files.stream().map(f -> upload(dirName, f)).collect(Collectors.toList());
    }

    public fileDto upload(String dirName, MultipartFile file){
        String originalName = file.getOriginalFilename();
        String extension = Optional.ofNullable(originalName)
                .filter(s -> s.contains("."))
                .map(s -> s.substring(originalName.lastIndexOf(".") + 1))
                .orElse(null);

        String fileName = dirName + "/" + UUID.randomUUID() + originalName;

        if(this.isImage(extension)) {
            try {
                s3Client.putObject(
                        PutObjectRequest.builder()
                                .key(fileName)
                                .bucket(bucket)
                                .build(), RequestBody.fromBytes(file.getBytes()));
//                amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), null));
            } catch(Exception e) {
                throw new S3FileUploadException("S3 파일 업로드 중 오류가 발생하였습니다.");
//                e.printStackTrace();
            }
        } else {
            throw new ImageTypeException("이미지 파일 형식은 png/jpeg/jpg/bmp/gif/svg 중 하나여야 합니다.");
        }

        //Dto형식으로 반환
        return fileDto.builder()
                .originalName(originalName)
                .extension(extension)
                .fileName(fileName)
                .url("https://roomimg.s3.ap-northeast-2.amazonaws.com/" + fileName)
//                .url(amazonS3Client.getUrl(bucket, fileName).toString())
                .build();
    }

    private boolean isImage(String extension) {
        return Optional.ofNullable(extension)
                .map(s -> s.toLowerCase().matches("png|jpeg|jpg|bmp|gif|svg"))
                .orElse(false);
    }
}