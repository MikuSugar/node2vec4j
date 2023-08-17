package me.mikusugar.node2vec;

import me.mikusugar.HelpTestUtils;
import me.mikusugar.word2vec.LoadModel;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author mikusugar
 * @version 1.0, 2023/8/4 09:42
 * @description
 */
public class Node2VecTest
{
    private final String karateModelPath = "karate_model.emb";

    @Test
    public void learnKarateTest() throws Exception
    {
        final String path = HelpTestUtils.getResourcePath() + "/karate.edgelist";
        final Graph graph = ParseUtils.edgeListFile2Graph(path, false);
        Node2VecLearn learn = new Node2VecLearn(1, 1, 80, 10, 128, 10, 1e-3, 0.025, 10, 10);
        learn.setNegative(10);
        learn.lean(graph);

        learn.saveBinaryMode(karateModelPath);
    }

    @Test
    public void node2vecKarateTest() throws IOException
    {
        LoadModel model = new LoadModel();
        model.loadBinaryMode(karateModelPath);

        String node = 22 + "";
        System.out.println(node + ":" + model.closestWords(node));

        final List<String> list = Arrays.asList("21", "22", "24");
        System.out.println(list + "::" + model.closestWords(list));
        model.getVecMap().forEach((k, v) -> System.out.println(k + "::" + Arrays.toString(v)));
    }

}
