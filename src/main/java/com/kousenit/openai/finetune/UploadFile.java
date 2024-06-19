package com.kousenit.openai.finetune;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.File;
import java.io.IOException;

public class UploadFile {
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");

    public static void main(String[] args) {
        File file = new File("mydata.jsonl");

        HttpPost uploadFile = new HttpPost("https://api.openai.com/v1/files");
        uploadFile.setHeader("Authorization", "Bearer " + API_KEY);

        HttpEntity entity = MultipartEntityBuilder.create()
                .setContentType(ContentType.MULTIPART_FORM_DATA)
                .addPart("purpose", new StringBody("fine-tune", ContentType.DEFAULT_TEXT))
                .addPart("file", new FileBody(file, ContentType.DEFAULT_BINARY))
                .build();

        uploadFile.setEntity(entity);

        HttpClientResponseHandler<String> responseHandler = response -> {
            int status = response.getCode();
            if (status >= 200 && status < 300) {
                HttpEntity responseEntity = response.getEntity();
                return responseEntity != null ? EntityUtils.toString(responseEntity) : null;
            } else {
                throw new IOException("Unexpected response status: " + status);
            }
        };

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String responseBody = httpClient.execute(uploadFile, responseHandler);
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            String fileId = jsonResponse.get("id").getAsString();
            System.out.println("File ID: " + fileId);
        } catch (IOException e) {
            throw new RuntimeException("Error uploading file", e);
        }
    }
}