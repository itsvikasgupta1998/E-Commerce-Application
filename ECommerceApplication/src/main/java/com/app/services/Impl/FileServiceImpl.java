package com.app.services.Impl;

import com.app.exceptions.FileStorageException;
import com.app.services.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

	private static final Set<String> ALLOWED_EXTENSIONS =
			Set.of("jpg", "jpeg", "png", "webp");

	@Value("${app.file.upload-dir}")
	private String uploadDir;

	// ---------------- UPLOAD ----------------
	@Override
	public String uploadImage(MultipartFile file) throws IOException {

		log.info("Upload request received. size={}",
				file != null ? file.getSize() : 0);

		validateFile(file);

		Files.createDirectories(Paths.get(uploadDir));


        String originalFileName = Objects.requireNonNull(file).getOriginalFilename();

		if (originalFileName == null || originalFileName.isBlank()) {
			log.warn("Invalid file name received from client");
			throw new FileStorageException("Invalid file name");
		}

		String extension = extractExtension(originalFileName);

		String storedFileName = UUID.randomUUID() + "." + extension;

		Path targetLocation = Paths.get(uploadDir).resolve(storedFileName);

		log.debug("Saving file at: {}", targetLocation.toAbsolutePath());

		Files.copy(file.getInputStream(), targetLocation,
				StandardCopyOption.REPLACE_EXISTING);

		log.info("File uploaded successfully. stored={}, original={}",
				storedFileName, originalFileName);

		return storedFileName;
	}

	// ---------------- GET FILE ----------------
	@Override
	public InputStream getResource(String fileName) throws IOException {

		log.info("File fetch request: {}", fileName);

		Path filePath = Paths.get(uploadDir)
				.resolve(fileName)
				.normalize();

		if (!Files.exists(filePath)) {
			log.error("File not found: {}", fileName);
			throw new FileStorageException("File not found: " + fileName);
		}

		log.debug("File located at: {}", filePath.toAbsolutePath());

		return Files.newInputStream(filePath);
	}

	// ---------------- DELETE FILE ----------------
	@Override
	public void deleteImage(String fileName) throws IOException {

		log.info("File delete request: {}", fileName);

		Path filePath = Paths.get(uploadDir)
				.resolve(fileName)
				.normalize();

		boolean deleted = Files.deleteIfExists(filePath);

		if (deleted) {
			log.info("File deleted successfully: {}", fileName);
		} else {
			log.warn("File not found for deletion: {}", fileName);
		}
	}

	// ---------------- VALIDATION ----------------
	private void validateFile(MultipartFile file) {

		if (file == null || file.isEmpty()) {
			log.warn("Validation failed: file is null/empty");
			throw new FileStorageException("File is empty");
		}

		String originalFileName = file.getOriginalFilename();

		// ✅ SAFE NULL CHECK (FIX FOR INTELLIJ WARNING)
		if (originalFileName == null || originalFileName.isBlank()) {
			log.warn("Validation failed: filename is null/blank");
			throw new FileStorageException("Invalid file name");
		}

		String extension = extractExtension(originalFileName);

		if (!ALLOWED_EXTENSIONS.contains(extension)) {
			log.warn("Invalid file type: {}", extension);
			throw new FileStorageException(
					"Only JPG, JPEG, PNG and WEBP files are allowed"
			);
		}

		log.debug("File validation passed: {}", originalFileName);
	}

	// ---------------- SAFE EXTENSION EXTRACTION ----------------
	private String extractExtension(String fileName) {

		int dotIndex = fileName.lastIndexOf(".");

		if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
			log.warn("Invalid file extension format: {}", fileName);
			throw new FileStorageException("Invalid file extension");
		}

		return fileName.substring(dotIndex + 1).toLowerCase();
	}
}