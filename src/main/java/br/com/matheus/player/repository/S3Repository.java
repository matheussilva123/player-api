package br.com.matheus.player.repository;

import br.com.matheus.player.exception.FileConverterException;
import br.com.matheus.player.exception.FileUploadException;
import br.com.matheus.player.utils.JsonConverter;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.InputStream;
import java.util.*;

@Repository
public class S3Repository {

    private static final String CONTENT_FILE_PATH = "content";

    @Value("${s3.bucket}")
    private String bucketName;

    private final AmazonS3 amazonS3;
    @Autowired
    private JsonConverter jsonConverter;

    public S3Repository(final AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public void uploadFile(final File file, final String path) {
        try {
            amazonS3.putObject(bucketName, String.format("/%s/%s", path, file.getName()), file);
        } catch (final SdkClientException e) {
            throw new FileUploadException(String.format("Failed to upload file. Exception: %s", e.getMessage()));
        }
    }

    public <T> List<T> get(final String path, final Class<? extends T> targetClass) {
        return get(bucketName, path, targetClass);
    }

    public <T> List<T> get(final String bucketName, final String path, final Class<? extends T> targetClass) {
        try {
            final String archiveString = getString(bucketName, path);

            return jsonConverter.toList(archiveString, targetClass);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getString(final String path) {
        return getString(bucketName, path);
    }

    public String getString(final String bucketName, final String path) {
        try {
            return amazonS3.getObjectAsString(bucketName, path);
        } catch (final AmazonS3Exception e) {
            if(e.getStatusCode() == 404) {
                return Collections.emptyList().toString();
            }
            throw new RuntimeException(e);
        }
    }

    public void put(final InputStream inputStream, final String filePath, final Map<String, String> userMetadata) {
        try {
            final ObjectMetadata metadata = new ObjectMetadata();
            metadata.setUserMetadata(Objects.requireNonNull(userMetadata));
            Optional.of(userMetadata)
                    .map(e -> e.get("Content-Type"))
                    .ifPresent(metadata::setContentType);

            amazonS3.putObject(bucketName, filePath, inputStream, metadata);
        } catch (final SdkClientException e) {
            throw new FileUploadException(String.format("Failed to upload file. Exception: %s", e.getMessage()));
        }
    }

    public List<String> listAllPaths() {
        final ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(bucketName)
                .withPrefix("/")
                .withDelimiter("/");

        final ObjectListing objects = amazonS3.listObjects(listObjectsRequest);

        final List<String> listPaths = Objects.requireNonNull(objects.getCommonPrefixes());

        return listPaths.stream().map(x -> x.replace("/", "")).toList();
    }

    public List<String> getSubFoldersByFolder(final String folder) {
        try {
            final ListObjectsRequest request = new ListObjectsRequest()
                .withPrefix(String.format("%s/%s/", CONTENT_FILE_PATH , folder))
                .withDelimiter("/")
                .withBucketName(bucketName);

            return extractToSubFoldersString(amazonS3.listObjects(request).getCommonPrefixes());
        } catch (final AmazonS3Exception e) {

            throw new FileConverterException(String.format("Failed to search files, error: %s", e.getMessage()));
        }
    }


    // Pegar /content/algum_folder/isso
    public List<String> getLists(final String prefix) {
        try {
            final List<String> filesListByPath = new ArrayList<>();

            final ListObjectsRequest listObjects = new ListObjectsRequest()
                    .withPrefix(prefix)
                    .withBucketName(bucketName);

            final List<S3ObjectSummary> filesList = amazonS3.listObjects(listObjects).getObjectSummaries();

            for (S3ObjectSummary value : filesList) {
                filesListByPath.add(value.getKey());
            }

            return filesListByPath;
        } catch (final Exception e) {
            throw new FileConverterException(String.format("Failed to search files, error: %s", e.getMessage()));
        }
    }

    public String getUrl(final String path){
        return amazonS3.getUrl(bucketName, path).toString();
    }

    public void uploadFileProper(final String path, final InputStream input,
                                 final ObjectMetadata metadata) {
        try {
            amazonS3.putObject(bucketName, path, input, metadata);
        } catch (final Exception e) {
            throw new FileUploadException(String.format("Failed to upload file. Exception: %s", e.getMessage()));
        }
    }

    private String getPathWithFileName(final String path, final String fileName) {
        return String.format("/%s/%s", path, fileName);
    }

    private boolean isEmpty(final String string) {
        return string == null || string.isEmpty() || string.isBlank();
    }

    private List<String> extractToSubFoldersString(final List<String> folders) {
        List<String> newSubFolders = new ArrayList<>();
        for (String folder : folders) {
            final int firstIndex = folder.indexOf("/");
            String newFolder = folder.substring(firstIndex + 1);
            if (newFolder.endsWith("/")) {
                newFolder = newFolder.substring(0, newFolder.length() - 1);
            }
            newSubFolders.add(newFolder);
        }
        return newSubFolders;
    }

}
