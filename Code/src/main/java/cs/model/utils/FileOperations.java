package cs.model.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class FileOperations {
    public static String getFileContent(String filePath) throws IOException {
        File f = new File(filePath);
        if (!f.exists())
            throw new RuntimeException("Read File Error: cannot find the file: " + filePath);
        return FileUtils.readFileToString(f, Charset.forName("utf-8"));
    }

    public static List<String> listFilesInDir(String dirPath){
        File dir = new File(dirPath);
        if (!dir.isDirectory())
            throw new RuntimeException("Not a directory");
        File[] fileList = dir.listFiles();
        List<String> paths = new ArrayList<>();
        if (fileList == null)
            return paths;
        for (File f: fileList){
            if (f.isFile()){
                paths.add(f.getAbsolutePath());
            }
        }
        return paths;
    }

    public static String getPackageFromBufferReader(BufferedReader br) throws IOException{
        String nextLine;
        String packageName = null;
        while ((nextLine = br.readLine()) != null) {
            if (nextLine.startsWith("package ")) {
                int sepIndex = nextLine.indexOf(";");
                if (sepIndex > 0) {
                    packageName = nextLine.substring(8, sepIndex);
                } else {
                    packageName = nextLine.substring(8);
                    if (packageName.endsWith("."))
                        packageName = packageName.substring(0, packageName.length()-1);
                }
                break;
            }
        }
        br.close();
        return packageName;
    }

    public static String getPackageOfFileReadFromDisk(String filePath) throws IOException{
        try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            return getPackageFromBufferReader(br);
        }
    }

    public static void serializeObjToFile(Serializable obj, String filePath) throws Exception{
        SerializationUtils.serialize(obj, new FileOutputStream(filePath));
    }

    public static Serializable deserializeObj(String filePath) throws Exception{
        return SerializationUtils.deserialize(new FileInputStream(filePath));
    }

}
