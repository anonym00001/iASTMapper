package cs.sysu.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CsvOperationsUtil {
    private static CSVParser getCsvParser(String csvPath) throws Exception{
        Reader reader = new BufferedReader(new FileReader(csvPath));
        return new CSVParser(reader, CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withIgnoreHeaderCase()
                .withTrim());
    }

    private static CSVPrinter getCsvPrinter(String csvPath, String[] headers) throws IOException{
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(csvPath));
        return new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers));
    }

    public static void writeCSV(String csvPath, String[] headers, List<String[]> data)throws IOException{
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(csvPath));
        CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers));
        for (String[] d: data){
            if (d.length != headers.length)
                throw new RuntimeException("data length and headers length do not match!");
            printer.printRecord((Object[])d);
        }
        printer.close();
        writer.close();
    }

    public static List<String[]> getCSVData(String csvPath, String[] headers) throws Exception{
        if (!new File(csvPath).exists())
            return null;

        CSVParser csvParser = getCsvParser(csvPath);
        List<String[]> dataRet = new ArrayList<>();
        for (CSVRecord csvRecord: csvParser){
            String[] d = new String[headers.length];
            for (int i = 0; i < headers.length; i++){
                d[i] = csvRecord.get(headers[i]);
            }
            dataRet.add(d);
        }
        return dataRet;
    }
}
