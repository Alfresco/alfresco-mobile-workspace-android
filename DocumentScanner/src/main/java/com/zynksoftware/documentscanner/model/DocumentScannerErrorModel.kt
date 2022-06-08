package com.zynksoftware.documentscanner.model

class DocumentScannerErrorModel {
    enum class ErrorMessage(val error: String) {
        INVALID_IMAGE("INVALID_IMAGE"),
        CROPPING_FAILED("CROPPING_FAILED");
    }
}
