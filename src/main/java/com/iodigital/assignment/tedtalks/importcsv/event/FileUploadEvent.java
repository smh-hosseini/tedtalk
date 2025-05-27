package com.iodigital.assignment.tedtalks.importcsv.event;

import com.iodigital.assignment.tedtalks.importcsv.model.ImportJob;

public record FileUploadEvent(ImportJob importJob) {
}
