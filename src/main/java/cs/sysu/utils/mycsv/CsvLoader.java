package cs.sysu.utils.mycsv;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CsvLoader {

    public static CsvData loadCsv(String filePath) throws IOException {
        CsvData data = new CsvData();
        List<String> lines = FileUtils.readLines(new File(filePath), "UTF-8");
        data.fromListOfLines(lines);
        return data;
    }
}
