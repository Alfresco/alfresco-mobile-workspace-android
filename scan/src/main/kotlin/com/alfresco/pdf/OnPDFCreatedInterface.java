package com.alfresco.pdf;

public interface OnPDFCreatedInterface {
    void onPDFCreated(boolean success, String path, String fileName);
}
