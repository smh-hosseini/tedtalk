package com.iodigital.assignment.tedtalks.talk.service;

import com.iodigital.assignment.tedtalks.importcsv.model.ImportJob;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface ImportJobService {

    ImportJob createImportJob(MultipartFile file);
    void createImportJobForClassPathResource(Resource resource);
}
