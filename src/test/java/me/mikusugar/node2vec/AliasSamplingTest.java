package me.mikusugar.node2vec;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mikusugar
 * @version 1.0, 2023/8/3 15:20
 * @description
 */
public class AliasSamplingTest
{
    private static final Logger logger = LoggerFactory.getLogger(AliasSamplingTest.class);

    @Test
    public void testAliasSampling()
    {
        final int[] nodes = {1, 2, 3, 4, 5};
        final int[] counts = {5, 1, 2, 3, 4};
        AliasSampling sampling = new AliasSampling(nodes, counts);
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < 1000_0000; i++)
        {
            final int next = sampling.next();
            map.put(next, map.getOrDefault(next, 0) + 1);
        }
        for (int i = 0; i < nodes.length; i++)
        {
            for (int j = i + 1; j < nodes.length; j++)
            {
                double expect = counts[j] * 1.0 / counts[i];
                double actual = map.get(nodes[j]) * 1.0 / map.get(nodes[i]);
                final double differenceRatio = Math.abs(expect - actual) / expect;
                logger.info("differenceRatio:{}", differenceRatio);
                assert differenceRatio <= 0.01;
            }
        }

    }
}
