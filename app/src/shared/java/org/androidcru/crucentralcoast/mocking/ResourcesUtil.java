package org.androidcru.crucentralcoast.mocking;

import java.io.InputStream;
import java.util.Scanner;

public class ResourcesUtil
{
    public static String getResourceAsString(String resourceName)
    {
        return convertStreamToString(ClassLoader.getSystemResourceAsStream(resourceName));
    }

    public static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
