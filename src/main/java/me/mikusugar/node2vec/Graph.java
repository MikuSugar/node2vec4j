package me.mikusugar.node2vec;

import it.unimi.dsi.fastutil.ints.*;

import java.util.*;

/**
 * @author mikusugar
 * @version 1.0, 2023/8/3 11:29
 * @description Graph
 */
public class Graph
{
    private final IntSet nodes;

    private final Int2ObjectMap<List<int[]>> edges;

    public Graph()
    {
        this.nodes = new IntOpenHashSet();
        this.edges = new Int2ObjectOpenHashMap<>();
    }

    public void addNode(int vid)
    {
        nodes.add(vid);
    }

    public IntCollection nodes()
    {
        return nodes;
    }

    public boolean contains(int vid)
    {
        return nodes.contains(vid);
    }

    public List<int[]> neighbors(int id)
    {
        return edges.getOrDefault(id, Collections.emptyList());
    }

    public void addEdge(int src, int dst)
    {
        if (nodes.contains(src) && nodes.contains(dst))
        {
            if (!edges.containsKey(src))
            {
                edges.put(src, new ArrayList<>());
            }
            edges.get(src).add(new int[] {src, dst, 1});
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    public void addEdge(int src, int dst, int wight)
    {
        if (nodes.contains(src) && nodes.contains(dst))
        {
            if (!edges.containsKey(src))
            {
                edges.put(src, new ArrayList<>());
            }
            edges.get(src).add(new int[] {src, dst, wight});
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    public boolean hasEdge(int src, int dst)
    {
        final List<int[]> edges = this.edges.get(src);
        if (edges == null)
        {
            return false;
        }
        for (int[] e : edges)
        {
            if (e[1] == dst)
            {
                return true;
            }
        }
        return false;
    }

}
