package com.iodigital.assignment.tedtalks.importcsv.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "import_jobs")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", unique = true, nullable = false)
    private String fileName;
    @Column(name = "file_hash", unique = true, nullable = false)
    private String fileHash;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(name = "file_path", nullable = false)
    private String filePath;
    private int lastProcessedLine;
    private int processedCount;
    private int skippedCount;
    private int failedCount;
    private int successfulCount;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Version
    private Long version;

    public enum Status {
        PENDING, IN_PROGRESS, COMPLETED, FAILED
    }

    public void processAndSucceed(){
        processed();
        this.successfulCount++;
    }

    public void processAndSkipped(){
        processed();
        this.skippedCount++;
    }

    public void processAndFailed(){
        processed();
        this.failedCount++;

    }

    private void processed() {
        this.processedCount++;
        this.lastProcessedLine++;
    }
}
