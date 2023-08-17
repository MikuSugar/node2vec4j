package me.mikusugar.node2vec;

import com.google.common.base.Preconditions;
import me.mikusugar.word2vec.SkipGram;
import me.mikusugar.word2vec.Word2Vec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author mikusugar
 * @version 1.0, 2023/8/3 17:06
 * @description
 */
public class Node2VecLearn
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /*
    超参数p，控制遍历的返回前一个节点的概率。
    */
    private double p = 1;

    /*
    超参数q，控制节点继续向前行的概率。
     */
    private double q = 1;

    /*
    每条路线上节点的数量
    */
    private int walkLength = 20;

    /*
    从头到尾反复遍历次数
    */
    private int numWalks = 10;

    /**
     * 训练多少个特征
     */
    private int layerSize = 242;

    /**
     * 上下文窗口大小
     */
    private int window = 5;

    private double sample = 1e-3;

    private double alpha = 0.025;

    private Word2Vec word2vec;

    private Map<Integer, double[]> nodeMap;

    private int MAX_EAP = 6;

    private int negative = 10;

    public Node2VecLearn(double p, double q, int walkLength, int numWalks, int layerSize, int window, double sample,
            double alpha, int MAX_EAP, int negative)
    {
        this.p = p;
        this.q = q;
        this.walkLength = walkLength;
        this.numWalks = numWalks;
        this.layerSize = layerSize;
        this.window = window;
        this.sample = sample;
        this.alpha = alpha;
        this.MAX_EAP = MAX_EAP;
        this.negative = negative;

        this.word2vec = new SkipGram();
        this.word2vec.setLayerSize(layerSize);
        this.word2vec.setWindow(window);
        this.word2vec.setSample(sample);
        this.word2vec.setAlpha(alpha);
        this.word2vec.setMAX_EXP(MAX_EAP);
        this.word2vec.setNegative(negative);
        check();
    }

    private void check()
    {
        Preconditions.checkArgument(this.window <= this.walkLength, "window参数必须小于walkLength");
        Preconditions.checkArgument(negative > 0, "negative必须大于0");
    }

    public Node2VecLearn()
    {

    }

    public void lean(Graph graph) throws Exception
    {
        check();
        final RandomWalk randomWalk = new RandomWalk(p, q, walkLength, numWalks, graph);
        File tempFile = null;
        try
        {
            tempFile = File.createTempFile("walks", ".txt");
            logger.info("creat temp file {}.", tempFile.getAbsolutePath());
            randomWalk.writeSimulateWalks(tempFile.getAbsolutePath());
            this.word2vec.fitFile(tempFile.getAbsolutePath(), 1);
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
                logger.info("delete temp file {}.", tempFile.getAbsolutePath());
            }
        }
    }

    public void saveBinaryMode(String path) throws IOException
    {
        this.word2vec.saveBinaryModel(path);
    }

    public void setP(double p)
    {
        this.p = p;
    }

    public void setQ(double q)
    {
        this.q = q;
    }

    public void setWalkLength(int walkLength)
    {
        this.walkLength = walkLength;
    }

    public void setNumWalks(int numWalks)
    {
        this.numWalks = numWalks;
    }

    public void setLayerSize(int layerSize)
    {
        this.layerSize = layerSize;
        this.word2vec.setLayerSize(layerSize);
    }

    public void setWindow(int window)
    {
        this.window = window;
        this.word2vec.setWindow(window);
    }

    public void setSample(double sample)
    {
        this.sample = sample;
        this.word2vec.setSample(sample);
    }

    public void setAlpha(double alpha)
    {
        this.alpha = alpha;
        this.word2vec.setAlpha(alpha);
    }

    public void setMAX_EAP(int MAX_EAP)
    {
        this.MAX_EAP = MAX_EAP;
        this.word2vec.setMAX_EXP(MAX_EAP);
    }

    public void setNegative(int negative)
    {
        this.negative = negative;
        this.word2vec.setNegative(negative);
    }
}
