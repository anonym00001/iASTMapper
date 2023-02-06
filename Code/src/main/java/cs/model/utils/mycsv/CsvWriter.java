package cs.model.utils.mycsv;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class CsvWriter {
    public static void writeCsv(CsvData data, String filePath) throws IOException {
        FileUtils.writeLines(new File(filePath), data.toListOfLines());
    }
}
