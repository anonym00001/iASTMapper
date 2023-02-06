package cs.model.utils.mycsv;

import java.util.*;

public class CsvRecord {
    private Map<String, String> headerValMap;
    private List<String> additionalEntries;

    public CsvRecord(){
        headerValMap = new HashMap<>();
        additionalEntries = new ArrayList<>();
    }

    public String getValueOfCol(String colName){
        return headerValMap.get(colName);
    }

    public List<String> getAdditionalEntries() {
        return additionalEntries;
    }

    public void setRecords(List<String> recordStr, String[] headers){
        for (int i = 0; i < headers.length; i++){
            if (recordStr.size() <= i)
                headerValMap.put(headers[i], "");
            else
                headerValMap.put(headers[i], recordStr.get(i));
        }

        if (recordStr.size() > headers.length){
            additionalEntries = recordStr.subList(headers.length, recordStr.size());
        }
    }

    public void fromStringRecord(String recordStr, String[] headers) {
        String[] strs = recordStr.split(",\\s*");
        List<String> strList = new ArrayList<>(Arrays.asList(strs));
        setRecords(strList, headers);
    }

    public String toStringRecord(String[] headers){
        String record = "";
        if (headerValMap.size() == 0)
            return record;
        record = headerValMap.get(headers[0]);
        for (int i = 1; i < headers.length; i++){
            record += ", ";
            record += headerValMap.get(headers[i]);
        }

        if (additionalEntries.size() > 0){

        }

        return record;
    }
}
