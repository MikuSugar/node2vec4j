package me.mikusugar.node2vec;

import java.util.Arrays;
import java.util.List;

/**
 * @author mikusugar
 * @version 1.0, 2023/8/3 16:02
 * @description
 */
public class RandomWalkTest
{
    public static void main(String[] args)
    {
        final Graph graph = creatGraph();
        RandomWalk randomWalk = new RandomWalk(1, 1, 5, 10, graph);
        final List<int[]> res = randomWalk.simulateWalks();
        for (int[] walks : res)
        {
            System.out.println(Arrays.toString(walks));
        }
    }

    private static Graph creatGraph()
    {
        Graph graph = new Graph();
        for (int i = 1; i <= 8; i++)
        {
            graph.addNode(i);
        }
        graph.addEdge(1, 2);
        graph.addEdge(1, 3);
        graph.addEdge(1, 4);
        graph.addEdge(1, 5);

        graph.addEdge(2, 1);

        graph.addEdge(3, 1);
        graph.addEdge(3, 4);

        graph.addEdge(4, 1);
        graph.addEdge(4, 3);

        graph.addEdge(5, 1);
        graph.addEdge(5, 6);
        graph.addEdge(5, 8);

        graph.addEdge(6, 5);
        graph.addEdge(6, 7);

        graph.addEdge(7, 6);
        graph.addEdge(7, 8);

        graph.addEdge(8, 5);
        graph.addEdge(8, 7);

        return graph;
    }
}
