package cs.sysu.evaluation.config;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 * Configuration class.
 */
public class MyConfig {
    private static final Configuration config = getConfiguration();

    // whether remove testing files when performing analysis
    private static final boolean removeTestFiles = getBooleanProperty("remove_test");

    // root path of the analyzed data
    private static final String rootPath = getStringProperty("root_path");

    // the maximum infinite loop time to check
    private static final int infiniteLoopTime = getIntegerProperty("infinite_loop_time");

    private static Configuration getConfiguration(){
        return getConfiguration("config.properties");
    }

    /**
     * Configuration file for each project
     *
     * The file is stored in "project_configs/$project$.config.properties"
     */
    private static Configuration getProjectConfiguration(String project){
        if (project.equals("tmp"))
            return null;
        return getConfiguration("project_configs/" + project + ".config.properties");
    }

    private static Configuration getConfiguration(String fileName){
        try {
            Parameters params = new Parameters();
//            System.out.println("File name is " + fileName);
            FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                    new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                            .configure(params.properties()
                                    .setFileName(fileName)
                                    .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));

            return builder.getConfiguration();
        } catch (ConfigurationException ex) {
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static String getStringProperty(String propertyName){
        return config.getString(propertyName);
    }

    private static boolean getBooleanProperty(String propertyName){
        return config.getBoolean(propertyName);
    }

    private static int getIntegerProperty(String propertyName){
        return config.getInt(propertyName);
    }

    private static String[] getStringArrayProperty(String propertyName){
        return config.getStringArray(propertyName);
    }

    public static boolean ifRemoveTestFiles(){
        return removeTestFiles;
    }

    public static String getRootPath(){
        return rootPath;
    }

    public static String getCloneUrl(String projectName){
        try {
            Configuration conf = getProjectConfiguration(projectName);
            return conf.getString("clone_url");
        } catch (Exception e){
            return "";
        }
    }

    private static String getCommitUrlPrefix(String projectName){
        Configuration conf = getProjectConfiguration(projectName);
        return conf.getString("github_commit_url_prefix");
    }

    /**
     * Get the GitHub URL for the analyzed commit
     * @param projectName the project
     * @param commitId the commit id
     */
    public static String getCommitUrl(String projectName, String commitId){
        return getCommitUrlPrefix(projectName) + commitId;
    }

    public static int getInfiniteLoopTime() {
        return infiniteLoopTime;
    }

}
