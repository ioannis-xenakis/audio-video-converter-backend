package com.johnxenakis.converter.storage.util;

import com.johnxenakis.converter.dto.StoredFileDto;
import com.johnxenakis.converter.storage.model.StoredFile;

public class StoredFileMapper {

    public static StoredFileDto toDto(StoredFile entity) {
        StoredFileDto dto = new StoredFileDto();

        dto.setId(entity.getId());
        dto.setBucket(entity.getBucket());
        dto.setObjectName(entity.getObjectName());
        dto.setContentType(entity.getContentType());
        dto.setSize(entity.getSize());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setOwnerId(entity.getOwnerId());
        dto.setTags(entity.getTags());

        return dto;
    }
}