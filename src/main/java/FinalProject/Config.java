package FinalProject;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Properties;


public class Config {
    private static final Logger logger = Logger.getLogger(Config.class);
    private static Properties props;

    public static final String EMBEDDED_ALGO_DIR = "embedded_algo_dir";
    public static final String ADDED_ALGORITHMS_DIR = "added_algorithms_dir";
    public static final String ADDED_ALGORITHMS_PACKAGE_DIR = "added_algorithms_package_dir";
    public static final String PROBLEMS_DIR = "problems_dir";
    public static final String REPORTS_OUT_DIR = "reports_out_dir";
    public static final String TITLE = "page_title";

    public static void loadConfig()
    {
        logger.debug("loading cong properties from file resources/conf.properties");
        File configFile = new File("resources/conf.properties");

        try {
            FileReader reader = new FileReader(configFile);
            props = new Properties();
            props.load(reader);

            reader.close();
        } catch (FileNotFoundException e) {
            logger.error("could not find configuration file", e);
        } catch (Exception e) {
            logger.error("failed loading the configuration file", e);
        }
    }


    public static String getStringPropery(String key, String profile)
    {
        logger.debug("getting property for key: " + profile + key);
        String result = props.getProperty(profile + key);
        if (result == null)
        {
            logger.error("could not find property " + profile + key);
        }
        return result;
    }

    public static String getStringPropery(String key)
    {
        String profile = getStringPropery("mode", "");
        return getStringPropery(key, profile);
    }



}
