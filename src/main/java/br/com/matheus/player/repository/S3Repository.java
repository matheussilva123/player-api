package br.com.matheus.player.repository;

import br.com.matheus.player.exception.FileConverterException;
import br.com.matheus.player.exception.FileUploadException;
import br.com.matheus.player.utils.JsonConverter;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.InputStream;

@Repository
public class S3Repository {

    private static final String CONTENT_FILE_PATH = "content";

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private final AmazonS3 amazonS3;

    private final JsonConverter jsonConverter;

    public S3Repository(final AmazonS3 amazonS3, final JsonConverter jsonConverter) {
        this.amazonS3 = amazonS3;
        this.jsonConverter = jsonConverter;
    }

    public <T> List<T> get(final String path, final Class<? extends T> targetClass) {
        return get(bucketName, path, targetClass);
    }

    public <T> List<T> get(final String bucketName, final String path, final Class<? extends T> targetClass) {
        try {
            final String archiveString = getString(bucketName, path);

            return jsonConverter.toList(archiveString, targetClass);
        } catch (final AmazonS3Exception e) {
            throw new RuntimeException(e);
        }
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

    public List<String> getAllFolders() {
        final ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(bucketName)
                .withPrefix("music/")
                .withDelimiter("/");

        final ObjectListing objects = amazonS3.listObjects(listObjectsRequest);

        final List<String> listPaths = Objects.requireNonNull(objects.getCommonPrefixes());

        return extractToFoldersString(listPaths);
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

    public String getUrl(final String path){
        return amazonS3.getUrl(bucketName, path).toString();
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

    private List<String> extractToFoldersString(final List<String> folders) {
        return folders.stream()
            .map(s -> s.substring(s.indexOf('/') + 1, s.lastIndexOf('/')))
            .toList();
    }

}
