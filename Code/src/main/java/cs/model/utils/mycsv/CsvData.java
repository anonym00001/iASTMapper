package cs.model.utils.mycsv;

import java.util.ArrayList;
import java.util.List;

public class CsvData {
    public String[] headers;
    public List<CsvRecord> records;

    public void fromListOfLines(List<String> lines){
        String headerStr = lines.get(0);
        headers = headerStr.split(",\\s*");
        records = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++){
            String line = lines.get(i);
            CsvRecord record = new CsvRecord();
            record.fromStringRecord(line, headers);
            records.add(record);
        }
    }

    public List<String> toListOfLines(){
        String firstLine = headers[0];
        for (int i = 1; i < headers.length; i++){
            firstLine += ", ";
            firstLine += headers[i];
        }
        List<String> ret = new ArrayList<>();
        ret.add(firstLine);
        for (CsvRecord record: records){
            ret.add(record.toStringRecord(headers));
        }
        return ret;
    }
}
