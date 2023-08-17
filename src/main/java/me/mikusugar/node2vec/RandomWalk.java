package me.mikusugar.node2vec;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author mikusugar
 * @version 1.0, 2023/8/3 13:54
 * @description 随机游走
 */
public class RandomWalk
{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /*
    超参数p，控制遍历的返回前一个节点的概率。
     */
    private double p;

    /*
    超参数q，控制节点继续向前行的概率。
     */
    private double q;

    /*
    每条路线上节点的数量
    */
    private int walkLength;

    /*
    从头到尾反复遍历次数
    */
    private int numWalks;

    /**
     * 需要随机游走的图
     */
    private final Graph graph;

    /**
     * 存储节点到邻居的可能性序列的结构<节点id，下一步选择器
     */
    private Int2ObjectMap<AliasSampling> aliasNodes;

    /**
     * 存储边的可能性序列的结构<上一个节点id,此时的节点id,下一步选择器
     */
    private Int2ObjectMap<Int2ObjectMap<AliasSampling>> aliasEdges;

    public RandomWalk(double p, double q, int walkLength, int numWalks, Graph graph)
    {
        this.p = p;
        this.q = q;
        this.walkLength = walkLength;
        this.numWalks = numWalks;
        this.graph = graph;
    }

    public List<int[]> simulateWalks()
    {
        aliasEdges = new Int2ObjectOpenHashMap<>();
        aliasNodes = new Int2ObjectOpenHashMap<>();
        final List<int[]> res = new ArrayList<>();
        for (int i = 0; i < numWalks; i++)
        {
            List<Integer> nodes = new ArrayList<>(graph.nodes());
            Collections.shuffle(nodes);
            for (int node : nodes)
            {
                int[] walk = node2vecWalk(node);
                res.add(walk);
            }
        }

        return res;
    }

    public void writeSimulateWalks(String path) throws IOException
    {
        final long startTime = System.currentTimeMillis();
        logger.info("write simulateWalks to {}.", path);
        aliasEdges = new Int2ObjectOpenHashMap<>();
        aliasNodes = new Int2ObjectOpenHashMap<>();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path)))
        {
            for (int i = 0; i < numWalks; i++)
            {
                List<Integer> nodes = new ArrayList<>(graph.nodes());
                Collections.shuffle(nodes);
                for (int node : nodes)
                {
                    final int[] walks = node2vecWalk(node);
                    for (int n : walks)
                    {
                        writer.write(n + " ");
                    }
                    writer.write(System.lineSeparator());
                }
            }
        }
        logger.info("write simulateWalks to {} success. take time:{}ms.", path, System.currentTimeMillis() - startTime);
    }

    private int[] node2vecWalk(int node)
    {
        final int[] walks = new int[walkLength];
        int idx = 0;
        walks[idx++] = node;
        while (idx < walkLength)
        {
            int cur = walks[idx - 1];
            final List<int[]> neighbors = graph.neighbors(cur);
            if (neighbors.isEmpty())
            {
                break;
            }
            else
            {
                if (idx == 1)
                {
                    int next = getNextStep(cur);
                    walks[idx++] = next;
                }
                else
                {
                    int preNode = walks[idx - 2];
                    int next = getNextStep(preNode, cur);
                    walks[idx++] = next;
                }
            }
        }
        if (idx < walkLength)
        {
            int[] ans = new int[idx];
            System.arraycopy(walks, 0, ans, 0, idx);
            return ans;
        }
        return walks;
    }

    private int getNextStep(int src, int dst)
    {
        Map<Integer, AliasSampling> srcAliasMap = aliasEdges.computeIfAbsent(src, k -> new Int2ObjectOpenHashMap<>());
        AliasSampling aliasSampling = srcAliasMap.get(dst);
        if (aliasSampling == null)
        {
            final List<int[]> dstNeighbors = graph.neighbors(dst);
            final double[] probability = new double[dstNeighbors.size()];
            final int[] nodes = new int[dstNeighbors.size()];
            for (int i = 0; i < dstNeighbors.size(); i++)
            {
                int nbr = dstNeighbors.get(i)[1];
                int wight = dstNeighbors.get(i)[2];
                nodes[i] = nbr;
                if (nbr == src)
                {
                    probability[i] = wight / p;
                }
                else if (graph.hasEdge(nbr, src))
                {
                    probability[i] = wight;
                }
                else
                {
                    probability[i] = wight / q;
                }
            }
            aliasSampling = new AliasSampling(nodes, probability);
            srcAliasMap.put(dst, aliasSampling);
        }
        return aliasSampling.next();
    }

    private int getNextStep(int node)
    {
        AliasSampling aliasSampling = aliasNodes.get(node);
        if (aliasSampling == null)
        {
            final List<int[]> neighbors = graph.neighbors(node);
            final int[] nodes = new int[neighbors.size()];
            final int[] weights = new int[neighbors.size()];
            for (int i = 0; i < neighbors.size(); i++)
            {
                final int[] cur = neighbors.get(i);
                nodes[i] = cur[1];
                weights[i] = cur[2];
            }
            aliasSampling = new AliasSampling(nodes, weights);
            aliasNodes.put(node, aliasSampling);
        }
        return aliasSampling.next();
    }

    public double getP()
    {
        return p;
    }

    public void setP(double p)
    {
        this.p = p;
    }

    public double getQ()
    {
        return q;
    }

    public void setQ(double q)
    {
        this.q = q;
    }

    public int getWalkLength()
    {
        return walkLength;
    }

    public void setWalkLength(int walkLength)
    {
        this.walkLength = walkLength;
    }

    public int getNumWalks()
    {
        return numWalks;
    }

    public void setNumWalks(int numWalks)
    {
        this.numWalks = numWalks;
    }
}
