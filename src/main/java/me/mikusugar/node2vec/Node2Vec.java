package me.mikusugar.node2vec;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static me.mikusugar.common.VectorUtils.calculateCosineSimilarity;

/**
 * @description
 * @author mikusugar
 * @version 1.0, 2023/8/4 10:28
 */
public class Node2Vec
{

    private final int topNSize = 10;

    public Node2Vec()
    {
    }

    private Map<Integer, double[]> nodeMap;

    public void loadEmbModel(String path) throws IOException
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(path)))
        {
            final String[] strs = reader.readLine().split(" ");
            final int size = Integer.parseInt(strs[0]);
            final int dimension = Integer.parseInt(strs[1]);
            this.nodeMap = new HashMap<>(size);
            for (int i = 0; i < size; i++)
            {
                final String[] s = reader.readLine().split(" ");
                final int node = Integer.parseInt(s[0]);
                final double[] vector = new double[dimension];
                for (int j = 0; j < dimension; j++)
                {
                    vector[j] = Double.parseDouble(s[j + 1]);
                }
                nodeMap.put(node, vector);
            }
        }
    }

    public List<NodeEntry> closestNodes(Collection<Integer> nodes)
    {
        if (nodes == null || nodes.isEmpty())
        {
            throw new IllegalArgumentException("nodes not empty!");
        }
        final double[] vectorSum = sumVector(nodes);
        final int size = Math.min(nodeMap.size() - 1, topNSize);
        Set<Integer> nodesSet = new HashSet<>(nodes);
        PriorityQueue<NodeEntry> pq = new PriorityQueue<>();
        nodeMap.forEach((curNode, curVector) ->
        {
            if (!nodesSet.contains(curNode))
            {
                final double score = calculateCosineSimilarity(vectorSum, curVector);
                pq.add(new NodeEntry(curNode, score));
                if (pq.size() > size)
                {
                    pq.poll();
                }
            }
        });
        return pq2list(pq);
    }

    private double[] sumVector(Collection<Integer> nodes)
    {
        double[] res = null;
        for (int node : nodes)
        {
            final double[] vector = nodeMap.get(node);
            if (vector == null)
            {
                throw new IllegalArgumentException("not found " + node);
            }
            if (res == null)
            {
                res = Arrays.copyOf(vector, vector.length);
            }
            else
            {
                for (int i = 0; i < vector.length; i++)
                {
                    res[i] += vector[i];
                }
            }
        }
        return res;
    }

    public List<NodeEntry> closestNodes(int node)
    {
        final double[] vector = nodeMap.get(node);
        if (vector == null)
        {
            throw new IllegalArgumentException("not found:" + node);
        }
        final PriorityQueue<NodeEntry> pq = new PriorityQueue<>();
        final int size = Math.min(nodeMap.size() - 1, topNSize);
        nodeMap.forEach((curNode, curVector) ->
        {
            if (curNode != node)
            {
                final double score = calculateCosineSimilarity(vector, curVector);
                pq.add(new NodeEntry(curNode, score));
                if (pq.size() > size)
                {
                    pq.poll();
                }
            }
        });
        return pq2list(pq);
    }

    private static List<NodeEntry> pq2list(PriorityQueue<NodeEntry> pq)
    {
        final List<NodeEntry> result = new ArrayList<>(pq.size());
        while (!pq.isEmpty())
        {
            result.add(pq.poll());
        }
        Collections.reverse(result);
        return result;
    }

    public Map<Integer, double[]> getNodeMap()
    {
        return nodeMap;
    }

}
