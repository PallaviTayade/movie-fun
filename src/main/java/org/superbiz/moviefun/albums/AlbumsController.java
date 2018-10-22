package org.superbiz.moviefun.albums;

import org.apache.tika.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;
    private final BlobStore blobStore;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public AlbumsController(AlbumsBean albumsBean, BlobStore blobStore) {
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

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        logger.debug("Uploading cover for album with id {}", albumId);
        saveUploadToFile(uploadedFile, getCoverFile(albumId));

        return format("redirect:/albums/%d", albumId);
    }

//    private void saveUploadToFile(@RequestParam("file") MultipartFile uploadedFile, File targetFile) throws IOException {
//
//
//        targetFile.delete();
//        targetFile.getParentFile().mkdirs();
//        targetFile.createNewFile();
//
//        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
//            outputStream.write(uploadedFile.getBytes());
//        }
//
//    }

    private void saveUploadToFile(@RequestParam("file") MultipartFile uploadedFile, String targetFile) throws IOException {
        try {
            Blob blob = new Blob (targetFile, uploadedFile.getInputStream(), uploadedFile.getContentType());
            blobStore.put(blob);
        } catch (IOException e) {
            logger.error("There was an error while uploading album cover", e);
        }
    }

//    @GetMapping("/{albumId}/cover")
//    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {
//        Path coverFilePath = getExistingCoverPath(albumId);
//        byte[] imageBytes = readAllBytes(coverFilePath);
//        HttpHeaders headers = createImageHttpHeaders(coverFilePath, imageBytes);
//
//        return new HttpEntity<>(imageBytes, headers);
//    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {

        Blob blob;
        HttpHeaders headers = new HttpHeaders();
        Optional <Blob> optBlob = getExistingCoverPath(albumId);

        if (optBlob.isPresent()) {
            blob = optBlob.get();
       }
        else {
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream input = classLoader.getResourceAsStream("default-cover.jpg");
            blob = new Blob("default-cover", input, IMAGE_JPEG_VALUE);
        }

        byte[] imageBytes = IOUtils.toByteArray(blob.inputStream);
        headers.setContentType(MediaType.parseMediaType(blob.contentType));
        headers.setContentLength(imageBytes.length);

        return new HttpEntity<>(imageBytes, headers);

    }



//    private HttpHeaders createImageHttpHeaders(Path coverFilePath, byte[] imageBytes) throws IOException {
//        String contentType = new Tika().detect(coverFilePath);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.parseMediaType(contentType));
//        headers.setContentLength(imageBytes.length);
//        return headers;
//    }

    private HttpHeaders createImageHttpHeaders(String contentType, byte[] imageBytes) throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }


//    private File getCoverFile(@PathVariable long albumId) {
//        String coverFileName = format("covers/%d", albumId);
//        return new File(coverFileName);
//    }

    private String getCoverFile(@PathVariable long albumId) {
        return format("covers/%d", albumId);
    }

    private Optional <Blob> getExistingCoverPath(@PathVariable long albumId) throws URISyntaxException, IOException {
        String coverFile = getCoverFile(albumId);

//        FileStore fileStore = new FileStore();

        Optional <Blob> blob = blobStore.get(coverFile);

        return blob;
    }
}
