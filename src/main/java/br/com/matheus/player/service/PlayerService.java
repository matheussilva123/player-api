package br.com.matheus.player.service;

import br.com.matheus.player.dto.AlbumDTO;
import br.com.matheus.player.dto.ArchiveDTO;
import br.com.matheus.player.repository.S3Repository;
import br.com.matheus.player.utils.JsonConverter;
import com.amazonaws.util.StringInputStream;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.xml.sax.ContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

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

    public List<String> getAllFolders() {
        return s3Repository.getAllFolders();
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

    public AlbumDTO getAlbumBy(final String folder) {
        if (checkIsNull(folder)) {
            throw new IllegalArgumentException("Folder cannot be null, empty or blank.");
        }
        final List<ArchiveDTO> archives = getArchivesByFolder(folder);
        final List<String> subFolders = getSubFoldersByFolder(folder);
        return new AlbumDTO(subFolders, folder, archives);
    }

    private void putArchive(final InputStream inputStream, final String pathFile,
                           final Map<String, String> contentType) {
        s3Repository.put(inputStream, pathFile, contentType);
    }

    private void putFileContent(final ArchiveDTO archive, final String folder) {
        try {
            final List<ArchiveDTO> archives = getArchivesByFolder(folder);
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
        final double duration = getDuration(multipartFile);
        return new ArchiveDTO(fileName, url, type, duration);
       }

    private boolean checkIsNull(final String string) {
        return string == null || string.isEmpty() || string.isBlank();
    }

    private List<String> getSubFoldersByFolder(final String folder) {
        return s3Repository.getSubFoldersByFolder(folder);
    }

    private List<ArchiveDTO> getArchivesByFolder(final String folder) {
        return s3Repository.get(buildContentFile(folder), ArchiveDTO.class);
    }

    private double getDuration(final MultipartFile multipartFile) {
        try {
            final ContentHandler handler = new BodyContentHandler();
            final Metadata metadata = new Metadata();
            final ParseContext parseCtx = new ParseContext();
            final InputStream input = new ByteArrayInputStream(multipartFile.getBytes());
            final Mp3Parser parser = new Mp3Parser();
            parser.parse(input, handler, metadata, parseCtx);

            return Double.parseDouble(metadata.get("xmpDM:duration"));
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
}
