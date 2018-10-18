package org.superbiz.moviefun.blobstore;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

public class FileStore implements BlobStore {

    @Override
    public void put(Blob blob) throws IOException {

        File coverFile = new File("covers/" + blob.name);
        coverFile.delete();
        coverFile.getParentFile().mkdirs();
        coverFile.createNewFile();

        try (FileOutputStream outputStream = new FileOutputStream(coverFile)) {
            byte[] bytes = IOUtils.toByteArray(blob.inputStream);
            outputStream.write(bytes);
        }

    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        File coverFile = new File("covers/" + name);
        if (coverFile.exists()) {

            Blob blob = new Blob(name, new FileInputStream(coverFile), new Tika().detect(coverFile));
            return Optional.of(blob);

        } else {
            return Optional.empty();
        }
    }

    @Override
    public void deleteAll() {
        // ...
    }

}
