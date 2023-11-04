package br.com.matheus.player.controller;

import br.com.matheus.player.dto.AlbumDTO;
import br.com.matheus.player.dto.PathDTO;
import br.com.matheus.player.service.PlayerService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin("*")
@RequestMapping(value = "/api/files", produces = {"application/json"})
@RestController
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(final PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/uploads")
    public void upload(@RequestParam("file") final MultipartFile file,
                       @RequestParam("path") final String path) {
                 playerService.put(file,path);
    }

    @PostMapping("/uploads/multiples")
    public void uploadFiles(@RequestParam("file") final List<MultipartFile> files,
                            @RequestParam("path") final String path) {
        playerService.uploadMultiFiles(files,path);
    }

    @GetMapping
    public AlbumDTO getAlbumsDTOByPath(@RequestBody final PathDTO pathDTO) {
        return playerService.getAlbumBy(pathDTO.getFolder());
    }

}
