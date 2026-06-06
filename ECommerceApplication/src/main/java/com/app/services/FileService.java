package com.app.services;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;


public interface FileService {

	String uploadImage(MultipartFile file) throws IOException;

	InputStream getResource(String fileName) throws IOException;

	void deleteImage(String fileName) throws IOException;
}