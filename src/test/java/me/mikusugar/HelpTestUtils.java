package me.mikusugar;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author mikusugar
 * @version 1.0, 2023/8/3 16:47
 * @description
 */
public class HelpTestUtils
{
    public static String getResourcePath()
    {
        Path resourceDirectory = Paths.get("src", "test", "resources");
        return resourceDirectory.toFile().getAbsolutePath();
    }
}
