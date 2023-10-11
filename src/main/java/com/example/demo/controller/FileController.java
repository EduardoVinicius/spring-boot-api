package com.example.demo.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.demo.entity.File;
import com.example.demo.service.FileService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/files")
@AllArgsConstructor
public class FileController {
	private FileService fileService;
	
	@PostMapping("/upload")
	public File uploadFile(@RequestParam("file") MultipartFile file) {
		String fileName = fileService.saveFile(file);
		
		String filePath = ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/files/downloadFile/")
				.path(fileName)
				.toUriString();
		
		return new File(fileName, filePath, file.getContentType(), file.getSize());
	}
	
	@GetMapping("/downloadFile/{fileName}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
		Resource resource = fileService.downloadFile(fileName);
		
		String contentType = fileService.getContentType(request, resource);
		
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}
}
