package me.mikusugar.node2vec;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

/**
 * @author mikusugar
 * @version 1.0, 2023/8/3 14:34
 * @description
 */
public class AliasSampling
{
    private final int[] nodes;

    private final int[] j;

    private final double[] q;

    private final Random random;

    public AliasSampling(int[] nodes, int[] weights)
    {
        this.nodes = nodes;
        this.j = new int[nodes.length];
        this.q = new double[nodes.length];
        this.random = new Random();
        //归一化
        final double[] probability = normalization(weights);

        setup(probability);
    }

    public AliasSampling(int[] nodes, double[] weights)
    {
        this.nodes = nodes;
        this.j = new int[nodes.length];
        this.q = new double[nodes.length];
        this.random = new Random();
        //归一化
        final double[] probability = normalization(weights);

        setup(probability);
    }

    private void setup(double[] probability)
    {
        int k = probability.length;
        final LinkedList<Integer> smaller = new LinkedList<>();
        final LinkedList<Integer> larger = new LinkedList<>();
        for (int i = 0; i < probability.length; i++)
        {
            q[i] = k * probability[i];
            if (q[i] < 1.0)
            {
                smaller.add(i);
            }
            else
            {
                larger.add(i);
            }
        }
        while (!smaller.isEmpty() && !larger.isEmpty())
        {
            int l = larger.poll();
            //noinspection DataFlowIssue
            int s = smaller.poll();
            j[s] = l;
            q[l] = q[l] - (1.0 - q[s]);
            if (q[l] < 1.0)
            {
                smaller.add(l);
            }
            else
            {
                larger.add(l);
            }
        }

    }

    private double[] normalization(int[] weights)
    {
        double[] doubleWeights = Arrays.stream(weights).asDoubleStream().toArray();
        return normalization(doubleWeights);
    }

    private double[] normalization(double[] weights)
    {
        double sum = Arrays.stream(weights).sum();
        double[] probability = new double[weights.length];
        for (int i = 0; i < weights.length; i++)
        {
            probability[i] = weights[i] / sum;
        }
        return probability;
    }

    public int next()
    {
        int idx = aliasDraw();
        return nodes[idx];
    }

    private int aliasDraw()
    {
        int k = j.length;
        int kk = random.nextInt(k);
        if (random.nextDouble() < q[kk])
        {
            return kk;
        }
        return j[kk];
    }
}
