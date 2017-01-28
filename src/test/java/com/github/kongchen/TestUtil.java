package com.github.kongchen;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;

public class TestUtil {

    private static final Logger LOG = Logger.getLogger(TestUtil.class);


    public static void deleteDirectory(File directory) {
        try {
            FileUtils.deleteDirectory(directory);
        } catch (Exception e) {
            LOG.warn("could not delete directory " + directory, e);
        }
    }

    public static void deleteFile(File file) {
        try {
            FileUtils.forceDelete(file);
        } catch (Exception e) {
            LOG.warn("Could not delete file " + file, e);
        }
    }

}
