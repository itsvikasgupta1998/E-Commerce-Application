package com.app.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.app.exceptions.FileStorageException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

	private static final Set<String> ALLOWED_EXTENSIONS =
			Set.of(
					"jpg",
					"jpeg",
					"png",
					"webp"
			);

	@Value("${app.file.upload-dir}")
	private String uploadDir;

	@Override
	public String uploadImage(
			MultipartFile file
	) throws IOException {

		validateFile(file);

		Files.createDirectories(
				Paths.get(uploadDir)
		);

		String originalFileName =
				file.getOriginalFilename();

		if (originalFileName == null ||
				originalFileName.isBlank()) {

			throw new FileStorageException(
					"Invalid file name"
			);
		}

		if (!originalFileName.contains(".")) {

			throw new FileStorageException(
					"File must contain a valid extension"
			);
		}

		String extension =
				originalFileName.substring(
						originalFileName.lastIndexOf(".") + 1
				).toLowerCase();

		String fileName =
				UUID.randomUUID() + extension;

		Path targetLocation =
				Paths.get(uploadDir)
						.resolve(fileName);

		Files.copy(
				file.getInputStream(),
				targetLocation,
				StandardCopyOption.REPLACE_EXISTING
		);

		log.info(
				"File uploaded successfully: {}",
				fileName
		);

		return fileName;
	}

	@Override
	public InputStream getResource(
			String fileName
	) throws IOException {

		Path filePath =
				Paths.get(uploadDir)
						.resolve(fileName)
						.normalize();

		if (!Files.exists(filePath)) {

			throw new FileStorageException(
					"File not found: " + fileName
			);
		}

		return Files.newInputStream(filePath);
	}

	private void validateFile(
			MultipartFile file
	) {

		if (file == null || file.isEmpty()) {

			throw new FileStorageException(
					"File is empty"
			);
		}

		String originalFileName =
				file.getOriginalFilename();

		if (originalFileName == null) {

			throw new FileStorageException(
					"Invalid file name"
			);
		}

		String extension =
				originalFileName.substring(
						originalFileName.lastIndexOf(".") + 1
				).toLowerCase();

		if (!ALLOWED_EXTENSIONS.contains(extension)) {

			throw new FileStorageException(
					"Only JPG, JPEG, PNG and WEBP files are allowed"
			);
		}
	}

	@Override
	public void deleteImage(
			String fileName
	) throws IOException {

		Path filePath =
				Paths.get(uploadDir)
						.resolve(fileName)
						.normalize();

		Files.deleteIfExists(filePath);

		log.info(
				"File deleted successfully: {}",
				fileName
		);
	}
}