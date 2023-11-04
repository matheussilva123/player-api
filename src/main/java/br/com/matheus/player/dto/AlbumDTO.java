package br.com.matheus.player.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AlbumDTO {

    private List<String> subFolders;
    private String folder;
    private List<ArchiveDTO> archivesDTO;

    public AlbumDTO() {
    }

    public AlbumDTO(
        @JsonProperty(value = "subFolders") List<String> subFolders,
        @JsonProperty(value = "folder") String folder,
        @JsonProperty(value = "archivesDTO") List<ArchiveDTO> archivesDTO) {
        this.subFolders = subFolders;
        this.folder = folder;
        this.archivesDTO = archivesDTO;
    }

    @JsonCreator
    public AlbumDTO(@JsonProperty(value = "folder") final String folder,
                    @JsonProperty(value = "archivesDTO") final List<ArchiveDTO> archivesDTO) {
        this.folder = folder;
        this.archivesDTO = archivesDTO;
    }

    @JsonCreator
    public AlbumDTO(@JsonProperty(value = "folder") final String folder) {
        this.folder = folder;
    }

    public String getFolder() {
        return folder;
    }

    public List<ArchiveDTO> getArchivesDTO() {
        return archivesDTO;
    }

    public List<String> getSubFolders() {
        return subFolders;
    }

    @Override
    public String toString() {
        return "AlbumDTO{" +
                "name='" + folder + '\'' +
                ", archivesDTO=" + archivesDTO +
                '}';
    }
}
