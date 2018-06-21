package com.ledgerleopard.sorvin.utils;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Utils {

    private static String POOL_CONFIG_NAME = "pool_config";

    public static String readFromAssets(Context context, String fileName ){

        try {
            BufferedInputStream open = new BufferedInputStream(context.getAssets().open(fileName));
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = open.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            return result.toString("UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static File writeToTmpFile( Context context, String content ) {
        File tempConfigFile = new File(context.getFilesDir().getAbsolutePath() + File.separator + "tmp.txn");
        tempConfigFile.deleteOnExit();

        try {
            tempConfigFile.createNewFile();
            FileWriter fw = new FileWriter(tempConfigFile);
            fw.write(content);
            fw.close();
        } catch (IOException e) {
            return null;
        }

        return tempConfigFile;
    }

    public static File writeConfigToTempFile(Context context, String configName ) {
        String content = readFromAssets(context, POOL_CONFIG_NAME + File.separator + configName);
        return writeToTmpFile(context, content);
    }

    public static String[] getPoolConfigNames(Context context ) {
        try {
            return context.getAssets().list(POOL_CONFIG_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
