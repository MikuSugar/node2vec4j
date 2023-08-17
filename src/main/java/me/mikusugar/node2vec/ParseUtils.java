package me.mikusugar.node2vec;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author mikusugar
 * @version 1.0, 2023/8/3 16:17
 * @description
 */
public class ParseUtils
{

    /**
     * Converts an edge list file to a graph. The edge list file should have each line representing an edge in the
     * graph, with two or three integers separated by whitespace. The first two integers represent the source and
     * destination nodes of the edge. The optional third integer represents the weight of the edge.
     *
     * @param path the path to the edge list file
     * @return the graph created from the edge list file
     * @throws IOException           if an I/O error occurs while reading the file
     * @throws IllegalStateException if a line in the file does not have two or three integers separated by whitespace
     */
    public static Graph edgeListFile2Graph(String path, boolean isDirection) throws IOException
    {
        Graph graph = new Graph();
        try (final BufferedReader reader = new BufferedReader(new FileReader(path)))
        {
            while (reader.ready())
            {
                final String line = reader.readLine();
                final String[] strs = line.split("[\\s　]+");
                if (strs.length != 2 && strs.length != 3)
                {
                    throw new IllegalStateException();
                }
                int src = Integer.parseInt(strs[0]);
                int dst = Integer.parseInt(strs[1]);
                graph.addNode(src);
                graph.addNode(dst);
                if (strs.length == 3)
                {
                    graph.addEdge(src, dst, Integer.parseInt(strs[2]));
                    if (!isDirection)
                    {
                        graph.addEdge(dst, src, Integer.parseInt(strs[2]));
                    }
                }
                else
                {
                    if (!isDirection)
                    {
                        graph.addEdge(src, dst);
                        graph.addEdge(dst, src);
                    }
                }
            }
        }
        return graph;
    }
}
