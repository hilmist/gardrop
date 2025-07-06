package com.hmert.imageUploadApi.client;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


import java.nio.file.Path;

@Component
public class ImageProcessorClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendImageToProcessor(String destinationFilePath, Path imagePath) {
        try {
            // Multipart body
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            body.add("image", new FileSystemResource(imagePath.toFile()));

            body.add("destinationFilePath", destinationFilePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            String url = "http://localhost:8081/images/process";

            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Image processor failed: " + response.getBody());
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to send image to processor", e);
        }
    }
}
