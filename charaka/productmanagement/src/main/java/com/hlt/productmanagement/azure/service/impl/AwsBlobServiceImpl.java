package com.hlt.productmanagement.azure.service.impl;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.hlt.productmanagement.azure.service.AwsBlobService;
import com.hlt.productmanagement.model.MediaModel;
import com.hlt.productmanagement.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AwsBlobServiceImpl implements AwsBlobService {

    @Value("${secretKey}")
    private String secretKey;

    @Value("${accessKey}")
    private String accessKey;

    @Value("${bucketName}")
    private String bucketName;

    @Value("${region}")
    private String region;

    @Autowired
    @Lazy
    private MediaService mediaService;

    @Override
    public AmazonS3 getClient() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.EU_NORTH_1)
                .build();
    }

    @Override
    public MediaModel uploadFile(MultipartFile file) throws IOException {
        MediaModel mediaModel = new MediaModel();
        File modified = new File(file.getOriginalFilename());
        try (FileOutputStream os = new FileOutputStream(modified)) {
            os.write(file.getBytes());
        }
        String fileName = System.currentTimeMillis() + "" + file.getOriginalFilename();
        getClient().putObject(bucketName, fileName, modified);
        //noinspection ResultOfMethodCallIgnored
        modified.delete();
        mediaModel.setFileName(fileName);
        mediaModel.setUrl(getS3Url(fileName));
        return mediaService.saveMedia(mediaModel);
    }

    @Override
    public List<MediaModel> uploadFiles(List<MultipartFile> files) throws IOException {
        List<MediaModel> uploaded = new ArrayList<>();
        for (MultipartFile file : files) {
            uploaded.add(uploadFile(file));
        }
        return uploaded;
    }

    @Override
    public MediaModel uploadCustomerPictureFile(Long customerId, MultipartFile file, Long createdUser) throws IOException {
        // Not used in productmanagement currently; implement as simple upload tagged to customer id
        MediaModel media = uploadFile(file);
        media.setCustomerId(customerId);
        return mediaService.saveMedia(media);
    }

    private String getS3Url(String fileName) {
        return "https://" + bucketName + "." + region + "/" + fileName;
    }
}
