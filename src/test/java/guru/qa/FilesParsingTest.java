package guru.qa;

import com.codeborne.pdftest.PDF;
import com.codeborne.pdftest.matchers.ContainsExactText;
import com.codeborne.selenide.Configuration;

import com.codeborne.xlstest.XLS;
import com.opencsv.CSVReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;


public class FilesParsingTest {

    ClassLoader cl = FilesParsingTest.class.getClassLoader();

    static {
        Configuration.browser = "firefox";
    }

    @DisplayName("Скачиваем и проверяем README.md")
    @Test
    void downloadTest() throws Exception {
        open("https://github.com/junit-team/junit5/blob/main/README.md");
        File textfile = $("#raw-url").download();
        try (InputStream is = new FileInputStream(textfile)) {
            byte[] fileContent = is.readAllBytes();
            String strContent = new String(fileContent, StandardCharsets.UTF_8);
            assertThat(strContent).contains("JUnit5");
        }
    }

    @DisplayName("Проверка количества страниц и содержимого PDF файла")
    @Test
    void pdfParsingTest() throws Exception {
        try (InputStream stream = cl.getResourceAsStream("pdf/sborka.pdf")) {
            PDF pdf = new PDF(stream);
            Assertions.assertEquals(48, pdf.numberOfPages);
            assertThat(pdf, new ContainsExactText("Термопаста"));
        }
    }

    @DisplayName("Проверка содержимого xls файла")
    @Test
    void xlsParsing() throws Exception {
        try (InputStream stream = cl.getResourceAsStream("xls/mobileCheckList.xlsx")) {
            XLS xls = new XLS(stream);
            String stringCellValue = xls.excel
                    .getSheetAt(1)
                    .getRow(3)
                    .getCell(1)
                    .getStringCellValue();
            org.assertj.core.api.Assertions.assertThat(stringCellValue).contains("realme UI");
        }
    }

    @DisplayName("Проверка содержимого csv файла")
    @Test
    void csvParsingTest() throws Exception {
        try (InputStream stream = cl.getResourceAsStream("csv/student.csv");
             CSVReader reader = new CSVReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            List<String[]> content = reader.readAll();
            org.assertj.core.api.Assertions.assertThat(content).contains(
                    new String[]{"Name", "Surname"},
                    new String[]{"Alexey", "Larin"}
            );
        }
    }

    @DisplayName("Проверка xls файла в zip")
    @Test
    void zipXlsParsingTest() throws Exception {
        ZipFile zf = new ZipFile(new File("src/test/resources/zip/mobileCheckList.zip"));
        try (ZipInputStream zis = new ZipInputStream(cl.getResourceAsStream("zip/mobileCheckList.zip"))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                org.assertj.core.api.Assertions.assertThat(entry.getName()).isEqualTo("mobileCheckList.xlsx");
                try (InputStream inputStream = zf.getInputStream(entry)) {
                    XLS xls = new XLS(inputStream);
                    String stringCellValue = xls.excel
                            .getSheetAt(1)
                            .getRow(3)
                            .getCell(1)
                            .getStringCellValue();
                    org.assertj.core.api.Assertions.assertThat(stringCellValue).contains("realme UI");
                }
            }
        }
    }

    @DisplayName("Проверка pdf файла в zip")
    @Test
    void zipPdfParseTest() throws Exception {
        ZipFile zf = new ZipFile(new File("src/test/resources/zip/sborka.zip"));
        try (ZipInputStream zis = new ZipInputStream(cl.getResourceAsStream("zip/sborka.zip"))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                org.assertj.core.api.Assertions.assertThat(entry.getName()).isEqualTo("sborka.pdf");
                try (InputStream InputStream = zf.getInputStream(entry)) {
                    PDF pdf = new PDF(InputStream);
                    Assertions.assertEquals(48, pdf.numberOfPages);
                    assertThat(pdf, new ContainsExactText("Термопаста"));
                }
            }
        }
    }

    @DisplayName("Проверка csv файла в zip")
    @Test
    void zipCsvParsingTest() throws Exception {
        ZipFile zf = new ZipFile(new File("src/test/resources/zip/student.zip"));
        try (ZipInputStream zis = new ZipInputStream(cl.getResourceAsStream("zip/student.zip"))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                org.assertj.core.api.Assertions.assertThat(entry.getName()).isEqualTo("student.csv");
                try (InputStream InputStream = zf.getInputStream(entry)) {
                    try (CSVReader reader = new CSVReader(new InputStreamReader(InputStream, StandardCharsets.UTF_8))) {
                        List<String[]> content = reader.readAll();
                        org.assertj.core.api.Assertions.assertThat(content).contains(
                                new String[]{"Name", "Surname"},
                                new String[]{"Alexey", "Larin"}
                        );
                    }
                }
            }
        }
    }
}