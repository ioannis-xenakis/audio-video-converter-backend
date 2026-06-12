package com.johnxenakis.converter.storage.service;

import com.johnxenakis.converter.storage.model.ResourceWithMeta;
import com.johnxenakis.converter.storage.model.StoredFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {

    StoredFile store(MultipartFile file, String ownerId, String tags) throws IOException;

    ResourceWithMeta load(String id);

    void delete(String id);
}
