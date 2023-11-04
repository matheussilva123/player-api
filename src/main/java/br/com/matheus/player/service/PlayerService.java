package br.com.matheus.player.service;

import br.com.matheus.player.dto.AlbumDTO;
import br.com.matheus.player.dto.ArchiveDTO;
import br.com.matheus.player.exception.FileConverterException;
import br.com.matheus.player.exception.ObjectNotFoundException;
import br.com.matheus.player.repository.S3Repository;
import br.com.matheus.player.utils.JsonConverter;
import com.amazonaws.util.StringInputStream;
import java.io.UnsupportedEncodingException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class PlayerService {

    private static final String JSON_TYPE = ".json";
    private static final Map<String, String> CONTENT_TYPE_APPLICATION_JSON =
            Collections.singletonMap("Content-Type", "application/json");
    private static final String CONTENT_MUSIC_PATH = "music";
    private static final String CONTENT_FILE_PATH = "content";

    private final S3Repository s3Repository;
    private final JsonConverter jsonConverter;

    public PlayerService(final S3Repository s3Repository, final JsonConverter jsonConverter) {
        this.s3Repository = s3Repository;
        this.jsonConverter = jsonConverter;
    }

    public void uploadFiles(final MultipartFile multipartFile, final String path) {
        if (checkIsNull(path)) {
            throw new IllegalArgumentException("Folder cannot be null, empty or blank.");
        }
        if(multipartFile.isEmpty()){
            throw new IllegalArgumentException("File cannot be empty or null");
        }

        final File file = convertMultipartFileToFile(multipartFile);
        s3Repository.uploadFile(file, path);
        file.delete();
    }

    public void uploadMultiFiles(final List<MultipartFile> multipartFile, final String path) {
        if(multipartFile.isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null, empty or blank.");
        }
        multipartFile.forEach(file -> uploadFiles(file, path));
    }

    public List<String> listAllPaths() {
        return s3Repository.listAllPaths();
    }

    public List<String> listFilesByPath(final String path) {
        if (checkIsNull(path)) {
            throw new IllegalArgumentException("Folder cannot be null, empty or blank.");
        }

        final List<String> filesList = s3Repository.getSubFoldersByFolder(path);

        if(filesList.isEmpty()) {
            throw new ObjectNotFoundException(String.format("%s not found.", path));
        }
        return filesList;
    }

    public void put(final MultipartFile multipartFile, final String folder) {
        try {
            final String fileName = multipartFile.getOriginalFilename();
            final InputStream inputStream = multipartFile.getInputStream();
            final Map<String, String> contentType =
                    Collections.singletonMap("Content-Type", multipartFile.getContentType());
            final String archivePath = buildPathArchive(folder, fileName);
            putArchive(inputStream, archivePath, contentType);
            putFileContent(buildArchiveDTO(multipartFile, folder), folder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getSubFoldersByFolder(final String folder) {
        return s3Repository.getSubFoldersByFolder(folder);
    }

    public AlbumDTO getAlbumBy(final String folder) {
        if (checkIsNull(folder)) {
            throw new IllegalArgumentException("Folder cannot be null, empty or blank.");
        }
        final List<ArchiveDTO> archives = getContentByFolder(folder);
        final List<String> subFolders = getSubFoldersByFolder(folder);
        return new AlbumDTO(subFolders, folder, archives);
    }

    private void putArchive(final InputStream inputStream, final String pathFile,
                           final Map<String, String> contentType) {
        s3Repository.put(inputStream, pathFile, contentType);
    }

    public List<ArchiveDTO> getContentByFolder(final String folder) {
        return s3Repository.get(buildContentFile(folder), ArchiveDTO.class);
    }

    public void putFileContent(final ArchiveDTO archive, final String folder) {
        try {
            final List<ArchiveDTO> archives = getContentByFolder(folder);
            if(archives.isEmpty()){
                s3Repository.put(convertToStringInputStream(Collections.singletonList(archive)),
                    buildContentFile(folder),
                    CONTENT_TYPE_APPLICATION_JSON);
            return ;
            }
            archives.add(archive);
            s3Repository.put(convertToStringInputStream(archives),
                buildContentFile(folder),
                CONTENT_TYPE_APPLICATION_JSON);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }


    private StringInputStream convertToStringInputStream(final List<ArchiveDTO> archiveDTOS) {
        try {
            return new StringInputStream(jsonConverter.toJson(archiveDTOS));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildPathArchive(final String folder, final String fileName) {
        return String.format("%s/%s/%s", CONTENT_MUSIC_PATH, folder, fileName);
    }

    private String buildContentFile(final String folder) {
        return String.format("%s/%s/%s%s", CONTENT_FILE_PATH, folder, extractFileNameJson(folder), JSON_TYPE);
    }

    private String extractFileNameJson(final String folder) {
        int lastIndex = folder.lastIndexOf("/");
        return folder.substring(lastIndex + 1);
    }

    private ArchiveDTO buildArchiveDTO(final MultipartFile multipartFile, final String folder) {
        final String fileName = multipartFile.getOriginalFilename();
        final String pathFile = buildPathArchive(folder, fileName);
        final String url = s3Repository.getUrl(pathFile);
        final String type = multipartFile.getContentType();

        return new ArchiveDTO(fileName, url, type, 0l);
       }

    private File convertMultipartFileToFile(final MultipartFile multipartFile) {
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(multipartFile.getBytes());
            fileOutputStream.close();
        } catch (final IOException e) {
            throw new FileConverterException(String.format("Failed to convert MultipartFile to file. Exception: %s",
                    e.getMessage()));
        }
        return file;
    }

    private boolean checkIsNull(final String string) {
        return string == null || string.isEmpty() || string.isBlank();
    }

}
