package com.jvm4csharp.generator;

import java.util.Arrays;

public final class TemplateHelper {
    public static final String SPACE;
    public static final char TAB;
    public static final String NEWLINE;
    public static final String BLOCK_OPEN;
    public static final String BLOCK_CLOSE;

    static {
        SPACE = " ";
        TAB = '\t';
        BLOCK_OPEN = "{";
        BLOCK_CLOSE = "}";
        NEWLINE = System.getProperty("line.separator");
    }

    public static String getIndentation(int level){
        char[] tabs = new char[level];
        Arrays.fill(tabs, TAB);
        return new String(tabs);
    }
}
