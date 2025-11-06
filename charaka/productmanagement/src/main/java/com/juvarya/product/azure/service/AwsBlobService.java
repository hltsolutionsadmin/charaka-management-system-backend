package com.juvarya.product.azure.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;


import com.juvarya.product.model.MediaModel;
import com.juvarya.product.repository.MediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AwsBlobService {

    @Value("${secretKey}")
    private String secretKey;

    @Value("${accessKey}")
    private String accessKey;

    @Value("${bucketName}")
    private String bucketName;

    @Value("${region}")
    private String region;

    @Autowired
    private MediaRepository mediaRepository;



    public AmazonS3 getClient() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.EU_NORTH_1).build();
    }

    public MediaModel uploadFile(MultipartFile file) throws IOException {
        MediaModel media = new MediaModel();
        File modified = new File(file.getOriginalFilename());
        FileOutputStream os = new FileOutputStream(modified);
        os.write(file.getBytes());
        String fileName = System.currentTimeMillis() + "" + file.getOriginalFilename();
        getClient().putObject(bucketName, fileName, modified);
        modified.delete();
        media.setFileName(fileName);
        media.setUrl(getS3Url(fileName));
        return mediaRepository.save(media);
    }

    public List<MediaModel> uploadFiles(List<MultipartFile> files) throws IOException {
        List<MediaModel> uploadFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            MediaModel media = uploadFile(file);
            uploadFiles.add(media);
        }
        return uploadFiles;
    }

    private String getS3Url(String fileName) {
        return "https://" + bucketName + "." + region + "/" + fileName;
    }



}
