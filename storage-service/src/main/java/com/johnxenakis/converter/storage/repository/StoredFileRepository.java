package com.johnxenakis.converter.storage.repository;

import com.johnxenakis.converter.storage.model.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredFileRepository extends JpaRepository<StoredFile, String> {
}
