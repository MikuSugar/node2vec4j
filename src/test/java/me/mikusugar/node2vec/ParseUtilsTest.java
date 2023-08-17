package me.mikusugar.node2vec;

import me.mikusugar.HelpTestUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author mikusugar
 * @version 1.0, 2023/8/3 16:50
 * @description
 */
public class ParseUtilsTest
{
    @Test
    public void testEdgeListFile2Graph() throws IOException
    {
        final String path = HelpTestUtils.getResourcePath() + "/karate.edgelist";
        final Graph graph = ParseUtils.edgeListFile2Graph(path, false);
        try (BufferedReader reader = new BufferedReader(new FileReader(path)))
        {
            while (reader.ready())
            {
                final String[] strs = reader.readLine().split("[\\s　]+");
                assert graph.hasEdge(Integer.parseInt(strs[0]), Integer.parseInt(strs[1]));
            }
        }
    }
}
