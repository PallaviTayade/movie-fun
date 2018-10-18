package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class S3Store implements BlobStore {
    private AmazonS3Client s3Client;
    private String bucket;

    public S3Store(AmazonS3Client s3Client, String photoStorageBucket) {
        this.s3Client = s3Client;
        bucket = photoStorageBucket;
    }

    @Override
    public void put(Blob blob) throws IOException {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(blob.contentType);
        s3Client.putObject(bucket, blob.name, blob.inputStream, objectMetadata);
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        if (s3Client.doesObjectExist(bucket, name)) {

            S3Object object = s3Client.getObject(bucket, name);

            Blob blob = new Blob(name,
                    object.getObjectContent(),
                    object.getObjectMetadata().getContentType());

            return Optional.of(blob);

        } else {
            return Optional.empty();
        }
    }

    @Override
    public void deleteAll() {
        ObjectListing objectListing = s3Client.listObjects(bucket);
        List<S3ObjectSummary> objectSummaries = objectListing.getObjectSummaries();
        objectSummaries.forEach(o -> s3Client.deleteObject(bucket, o.getKey()));
    }

}
