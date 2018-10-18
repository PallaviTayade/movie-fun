package org.superbiz.moviefun.albums;

import org.apache.tika.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;
    private final BlobStore blobStore;

    public AlbumsController(AlbumsBean albumsBean,
                            BlobStore blobStore) {
        this.albumsBean = albumsBean;
        this.blobStore = blobStore;
    }

    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    // ------

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {

        Blob blob = new Blob(Long.toString(albumId), uploadedFile.getInputStream(), uploadedFile.getContentType());
        blobStore.put(blob);

        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException {

        Optional<Blob> optional = blobStore.get(Long.toString(albumId));
        Blob blob;
        if (optional.isPresent()) {
            blob = optional.get();
        } else {
            blob = getExistingCover();
        }

        byte[] imageBytes = IOUtils.toByteArray(blob.inputStream);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(blob.contentType));
        headers.setContentLength(imageBytes.length);

        return new HttpEntity<>(imageBytes, headers);
    }

    private Blob getExistingCover() {
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("default-cover.jpg");
        return new Blob("default cover", resourceAsStream, "image/jpg");
    }
}
