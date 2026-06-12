package com.johnxenakis.converter.storage.model;

import java.io.InputStream;

public class ResourceWithMeta {

    private StoredFile meta;
    private InputStream inputStream;

    public StoredFile getMeta() {
        return meta;
    }

    public void setMeta(StoredFile meta) {
        this.meta = meta;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
