package com.ecommerce.project.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
@Service
public class FileServiceImpl implements FileService {
	@Override
	public String uploadImage(String path, MultipartFile file) throws IOException {

	    String originalFileName = file.getOriginalFilename();

	    String randomId = UUID.randomUUID().toString();
	    String fileName = randomId.concat(
	            originalFileName.substring(originalFileName.lastIndexOf('.'))
	    );

	    Path filePath = Paths.get(path.trim(), fileName);

	    File folder = new File(path.trim());
	    if (!folder.exists()) {
	        folder.mkdirs();
	    }

	    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

	    return fileName;
	}
}
