package com.test.spring.boot.demo;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.Test;

public class FileTestCase {

	@Test
	public void testFileUpload() throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();

		HttpPost httppost = new HttpPost("http://localhost:8080/uploadFile");
		File file = new File("C://workspace//spring-boot-demo//sample_input.txt");

		MultipartEntityBuilder mpEntity = MultipartEntityBuilder.create();
		mpEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		mpEntity.addPart("file", new FileBody(file));

		JSONObject json = new JSONObject();
		json.put("UUID", UUID.randomUUID().toString());
		json.put("fileName", "sample.txt");

		mpEntity.addPart("metaData", new StringBody(json.toString()));

		httppost.setEntity(mpEntity.build());

		System.out.println("executing request " + httppost.getRequestLine());
		HttpResponse response = httpclient.execute(httppost);

		HttpEntity resEntity = response.getEntity();

		System.out.println("Response status " + response.getStatusLine());
		if (resEntity != null) {
			System.out.println("Response Body " + EntityUtils.toString(resEntity));
		}
	}

}
