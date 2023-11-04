package br.com.matheus.player.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PathDTO {

  private final String folder;

  @JsonCreator
  public PathDTO(@JsonProperty("folder") final String folder) {
    this.folder = folder;
  }

  public String getFolder() {
    return folder;
  }

  @Override
  public String toString() {
    return "PathDTO{" +
        "folder='" + folder + '\'' +
        '}';
  }
}
