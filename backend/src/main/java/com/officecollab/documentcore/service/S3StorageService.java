package com.officecollab.documentcore.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;

/** Thin wrapper around the S3 SDK: upload version bytes, and mint short-lived presigned GET URLs. */
@Service
public class S3StorageService {

    private static final Duration PRESIGNED_URL_TTL = Duration.ofMinutes(10);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;

    public S3StorageService(S3Client s3Client,
                             S3Presigner s3Presigner,
                             @Value("${aws.s3.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
    }

    public void upload(String key, byte[] content, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .contentLength((long) content.length)
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(content));
    }

    /**
     * A short-TTL presigned GET URL for the given key. Never return a permanent/public S3 URL to a client --
     * this is the only way document bytes leave S3 for the ONLYOFFICE editor to load.
     */
    public String presignedGetUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(PRESIGNED_URL_TTL)
                .getObjectRequest(getObjectRequest)
                .build();
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    /**
     * Raw object bytes. Used to proxy document downloads through our own backend (see
     * OnlyOfficeCallbackController#content) rather than handing ONLYOFFICE a presigned S3 URL directly --
     * ONLYOFFICE attaches its own "Authorization: Bearer <jwt>" header to every outbound request it makes,
     * which collides with S3's query-string SigV4 auth and S3 rejects the request with 400.
     */
    public byte[] download(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return s3Client.getObject(request, ResponseTransformer.toBytes()).asByteArray();
    }
}
