package com.team.autoplugin.comm;

public class PropertiesPath {

    public static final String PREFIX = "META-INF/gradle-plugins/";

    public static String parsePath(String pluginId) {
        return PREFIX + pluginId + ".properties";
    }

}
