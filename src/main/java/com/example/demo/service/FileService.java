package com.example.demo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.config.FileStorageProperties;
import com.example.demo.exception.FileNotFoundException;
import com.example.demo.exception.FileUploadException;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileService {
	
	private final Path fileStorageLocation;
	
	public FileService(FileStorageProperties fileStorageProperties) {
		this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDirectory())
				.toAbsolutePath().normalize();
		
		try {
			Files.createDirectories(this.fileStorageLocation);
		} catch (IOException e) {
			throw new FileUploadException("Something went wrong while trying to create the directory.", e);
		}
	}
	
	public Resource downloadFile(String fileName) {
		try {
			Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
			Resource resource = new UrlResource(filePath.toUri());
			if (resource.exists()) {
				return resource;
			} else {
				throw new FileNotFoundException("File not found");
			}
		} catch (Exception e) {
			throw new FileNotFoundException("File not found.");
		}
	}
	
	public String getContentType(HttpServletRequest request, Resource resource) {
		String contentType = null;
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		} catch (IOException e) {
			log.error("Can't determine the file type.");
		}
		
		if (contentType == null) {
			contentType = "application/octet-stream";
		}
		
		return contentType;
	}
	
	public String saveFile(MultipartFile file) {
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		
		try {
			if (fileName.contains("..")) {
				throw new FileUploadException("Invalid file");
			}
			Path targetLocation = this.fileStorageLocation.resolve(fileName);
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
			
			return fileName;
			
		} catch (IOException e) {
			throw new FileUploadException("Something went wrong while trying to save the file", e);
		}
	}
}
