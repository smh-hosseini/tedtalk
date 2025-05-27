package com.iodigital.assignment.tedtalks.processor;

import com.iodigital.assignment.tedtalks.importcsv.reader.TedTalkCSVReader;
import com.iodigital.assignment.tedtalks.importcsv.model.TedTalkRecord;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TedTalkCSVReaderTest {

    @Test
    void constructor_WithValidHeaders_ShouldInitializeCorrectly() {
        // Arrange
        String csvContent = "title,author,date,views,likes,link\n";
        StringReader reader = new StringReader(csvContent);

        // Act & Assert
        assertDoesNotThrow(() -> new TedTalkCSVReader(reader));
    }

    @Test
    void constructor_WithMissingRequiredHeader_ShouldThrowException() {
        // Arrange
        String csvContent = "title,speaker,date,views,link\n"; // missing 'likes' header
        StringReader reader = new StringReader(csvContent);

        // Act & Assert
        CsvValidationException exception = assertThrows(CsvValidationException.class, () -> new TedTalkCSVReader(reader));
        assertTrue(exception.getMessage().contains("Missing required headers"));
    }

    @Test
    void constructor_WithEmptyFile_ShouldThrowException() {
        // Arrange
        String csvContent = "";
        StringReader reader = new StringReader(csvContent);

        // Act & Assert
        CsvValidationException exception = assertThrows(CsvValidationException.class, () -> new TedTalkCSVReader(reader));
        assertEquals("CSV file is empty - no headers found", exception.getMessage());
    }


   @Test
   void readNextTedTalk_WithComplexTitles_ShouldHandleEmbeddedQuotes() throws IOException, CsvException {
       // Arrange
       String csvContent = "title,author,date,views,likes,link\n" +
               "\"How to fix the \"\"bugs\"\" in the net-zero code\",Lucas Joppa,October 2021,526000,15000,https://ted.com/talks/lucas_joppa_how_to_fix_the_bugs_in_the_net_zero_code\n" +
               "\"\"\"Big Yellow Taxi\"\" / \"\"Song for Sunshine\"\"\",Belle and Sebastian,October 2021,23000,690,https://ted.com/talks/belle_and_sebastian_big_yellow_taxi_song_for_sunshine";

       StringReader reader = new StringReader(csvContent);
       TedTalkCSVReader csvReader = new TedTalkCSVReader(reader);

       // Act - First record
       TedTalkRecord firstTalk = csvReader.readNextTedTalk();

       // Assert - First record
       assertNotNull(firstTalk);
       assertEquals("How to fix the \"bugs\" in the net-zero code", firstTalk.getTitle());
       assertEquals("Lucas Joppa", firstTalk.getSpeaker());
       assertEquals(LocalDate.of(2021, 10, 1), firstTalk.getDate());
       assertEquals(526000L, firstTalk.getViews());
       assertEquals(15000L, firstTalk.getLikes());
       assertEquals("https://ted.com/talks/lucas_joppa_how_to_fix_the_bugs_in_the_net_zero_code", firstTalk.getLink());

       // Act - Second record
       TedTalkRecord secondTalk = csvReader.readNextTedTalk();

       // Assert - Second record
       assertNotNull(secondTalk);
       assertEquals("\"Big Yellow Taxi\" / \"Song for Sunshine\"", secondTalk.getTitle());
       assertEquals("Belle and Sebastian", secondTalk.getSpeaker());
       assertEquals(LocalDate.of(2021, 10, 1), secondTalk.getDate());
       assertEquals(23000L, secondTalk.getViews());
       assertEquals(690L, secondTalk.getLikes());
       assertEquals("https://ted.com/talks/belle_and_sebastian_big_yellow_taxi_song_for_sunshine", secondTalk.getLink());
   }

     @Test
    void readNextTedTalk_WithValidRow_ShouldReturnTedTalkRecor() throws IOException, CsvException {
        // Arrange
        String csvContent = """
                title,author,date,views,likes,link
                How to learn anything fast,Josh Kaufman,October 2021,12345678,98765,https://tedtalk.whatever.com
            """;
        StringReader reader = new StringReader(csvContent);
        TedTalkCSVReader csvReader = new TedTalkCSVReader(reader);

        // Act
        TedTalkRecord tedTalk = csvReader.readNextTedTalk();

        // Assert
        assertNotNull(tedTalk);
        assertEquals("How to learn anything fast", tedTalk.getTitle());
        assertEquals("Josh Kaufman", tedTalk.getSpeaker());
        assertEquals(LocalDate.of(2021, 10, 1), tedTalk.getDate());
        assertEquals(12345678L, tedTalk.getViews());
        assertEquals(98765L, tedTalk.getLikes());
        assertEquals("https://tedtalk.whatever.com", tedTalk.getLink());
    }

    @Test
    void readNextTedTalk_WithMissingField_ShouldThrowException() throws IOException, CsvValidationException {
        // Arrange
        String csvContent = """
                title,author,date,views,likes,link
                How to learn anything fast,,October 2021,12345678,98765,https://tedtalk.whatever.com
                """; // missing author
        StringReader reader = new StringReader(csvContent);
        TedTalkCSVReader csvReader = new TedTalkCSVReader(reader);
        var tedTalkRecord = csvReader.readNextTedTalk();
        // Act & Assert
        assertTrue(tedTalkRecord.isEmpty(), "Expected TedTalkRecord to be empty due to missing author field");
    }

    @Test
    void readBatch_WithMultipleRecords_ShouldReturnCorrectNumberOfRecords() throws IOException, CsvValidationException {
        // Arrange
        String csvContent = """
                title,author,date,views,likes,link
                Talk 1,Speaker 1,January 2020,1000,500,https://link1.com
                Talk 2,Speaker 2,February 2021,2000,600,https://link2.com
                Talk 3,Speaker 3,March 2022,3000,700,https://link3.com
                """;
        StringReader reader = new StringReader(csvContent);
        TedTalkCSVReader csvReader = new TedTalkCSVReader(reader);

        // Act
        List<TedTalkRecord> batch = csvReader.readBatch(10);

        // Assert
        assertEquals(3, batch.size());
        assertEquals("Talk 1", batch.get(0).getTitle());
        assertEquals("Talk 2", batch.get(1).getTitle());
        assertEquals("Talk 3", batch.get(2).getTitle());
    }

    @Test
    void readBatch_WithBatchSizeSmallerThanAvailableRecords_ShouldRespectBatchSize() throws IOException, CsvValidationException {
        // Arrange
        String csvContent = """
                title,author,date,views,likes,link
                Talk 1,Speaker 1,January 2020,1000,500,https://link1.com
                Talk 2,Speaker 2,February 2021,2000,600,https://link2.com
                Talk 3,Speaker 3,March 2022,3000,700,https://link3.com
                """;
        StringReader reader = new StringReader(csvContent);
        TedTalkCSVReader csvReader = new TedTalkCSVReader(reader);

        // Act
        List<TedTalkRecord> batch = csvReader.readBatch(2);

        // Assert
        assertEquals(2, batch.size());
        assertEquals("Talk 1", batch.get(0).getTitle());
        assertEquals("Talk 2", batch.get(1).getTitle());

        // Next batch should have the remaining record
        List<TedTalkRecord> nextBatch = csvReader.readBatch(2);
        assertEquals(1, nextBatch.size());
        assertEquals("Talk 3", nextBatch.getFirst().getTitle());
    }

    @Test
    void readBatch_WithNoMoreRecords_ShouldReturnEmptyList() throws IOException, CsvValidationException {
        // Arrange
        String csvContent = """
                title,author,date,views,likes,link
                Talk 1,Speaker 1,January 2020,1000,500,https://link1.com
                """;
        StringReader reader = new StringReader(csvContent);
        TedTalkCSVReader csvReader = new TedTalkCSVReader(reader);

        // Read the only record first
        csvReader.readBatch(1);

        // Act
        List<TedTalkRecord> emptyBatch = csvReader.readBatch(1);

        // Assert
        assertTrue(emptyBatch.isEmpty());
    }
}