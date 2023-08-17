package me.mikusugar.node2vec;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mikusugar
 * @version 1.0, 2023/8/3 15:20
 * @description
 */
public class AliasSamplingTest
{
    public static void main(String[] args)
    {
        AliasSampling sampling = new AliasSampling(new int[] {1, 2, 3, 4, 5}, new int[] {5, 1, 2, 3, 4});
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < 1500000; i++)
        {
            final int next = sampling.next();
            map.put(next, map.getOrDefault(next, 0) + 1);
        }
        map.forEach((k, v) -> System.out.println(k + "::" + v));
    }
}
