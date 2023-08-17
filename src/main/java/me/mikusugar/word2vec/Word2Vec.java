package me.mikusugar.word2vec;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.mikusugar.node2vec.AliasSampling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description
 * @author mikusugar
 * @version 1.0, 2023/8/14 11:10
 */
public abstract class Word2Vec
{
    private static final Logger logger = LoggerFactory.getLogger(Word2Vec.class);

    protected Object2IntMap<String> word2idx;

    protected ObjectList<Word> idx2Word;

    protected double[][] syn0;

    protected double[][] syn1;

    /**
     * 训练多少个特征
     */
    protected int layerSize = 200;

    /**
     * 上下文窗口大小
     */
    protected int window = 5;

    protected double sample = 1e-3;

    protected double alpha = 0.025;

    protected double startingAlpha = alpha;

    public int EXP_TABLE_SIZE = 1000;

    protected final double[] expTable = new double[EXP_TABLE_SIZE];

    protected int trainWordsCount = 0;

    protected int MAX_EXP = 6;

    protected int negative = 5;

    protected final Random random = new Random();

    protected AliasSampling negativeSampling;

    public Word2Vec()
    {

    }

    protected AtomicInteger wordCount = new AtomicInteger(0);

    protected int lastWordCount = 0;

    protected int wordCountActual = 0;

    protected void updateLearRate()
    {
        int wordCount = this.wordCount.get();
        if (wordCount - lastWordCount > 10000)
        {
            logger.info("alpha: {},Progress: {}%", alpha,
                    String.format("%.4f", (wordCountActual / (double)(trainWordsCount)) * 100));

            wordCountActual += wordCount - lastWordCount;
            lastWordCount = wordCount;
            alpha = startingAlpha * (1 - wordCountActual / (double)(trainWordsCount + 1));
            if (alpha < startingAlpha * 0.0001)
            {
                alpha = startingAlpha * 0.0001;
            }
        }
    }

    protected IntList getSentence(String[] strs)
    {
        IntList sentence = new IntArrayList(strs.length);
        for (String str : strs)
        {
            final int idx = word2idx.getOrDefault(str, -1);
            if (idx == -1)
            {
                continue;
            }

            if (sample > 0)
            {
                // @formatter:off
                double ran = (Math.sqrt(idx2Word.get(idx).getCount() * 1.0 / (sample * trainWordsCount)) + 1)
                        * (sample * trainWordsCount) / idx2Word.get(idx).getCount();
                // @formatter:on
                if (ran < random.nextDouble())
                {
                    continue;
                }
            }
            sentence.add(idx);
        }
        return sentence;
    }

    protected double getGradient(int input, int target, int label)
    {
        double f = dot(syn0[input], syn1[target]);
        double g;
        if (f > MAX_EXP)
        {
            g = (label - 1) * alpha;
        }
        else if (f < -MAX_EXP)
        {
            g = label * alpha;
        }
        else
        {
            g = (label - expTable[(int)((f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))]) * alpha;
        }
        return g;
    }

    protected double dot(double[] vec1, double[] vec2)
    {
        Preconditions.checkArgument(vec1.length == vec2.length);
        double f = 0d;
        for (int j = 0; j < vec1.length; j++)
        {
            f += vec1[j] * vec2[j];
        }
        return f;
    }

    protected void readVocab(File file) throws IOException
    {
        logger.info("read vocab filePath:{}...", file.getAbsolutePath());
        this.idx2Word = new ObjectArrayList<>();
        this.word2idx = new Object2IntOpenHashMap<>();

        final long startTime = System.currentTimeMillis();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()))))
        {
            String temp;
            while ((temp = br.readLine()) != null)
            {
                String[] split = temp.split("[\\s　]+");
                trainWordsCount += split.length;
                for (String word : split)
                {
                    if (word2idx.containsKey(word))
                    {
                        idx2Word.get(word2idx.getInt(word)).incrementCount();
                    }
                    else
                    {
                        word2idx.put(word, idx2Word.size());
                        idx2Word.add(new Word(word));
                    }
                }
            }
        }
        logger.info("read vocab file success! take time:{}ms", System.currentTimeMillis() - startTime);
    }

    /**
     * Precompute the exp() table f(x) = x / (x + 1)
     */
    protected void createExpTable()
    {
        for (int i = 0; i < EXP_TABLE_SIZE; i++)
        {
            expTable[i] = Math.exp(((i / (double)EXP_TABLE_SIZE * 2 - 1) * MAX_EXP));
            expTable[i] = expTable[i] / (expTable[i] + 1);
        }
    }

    public abstract void fitFile(String filePath, int threads) throws Exception;

    protected void initNet()
    {
        this.syn0 = new double[idx2Word.size()][layerSize];
        this.syn1 = new double[idx2Word.size()][layerSize];

        for (int i = 0; i < syn0.length; i++)
        {
            for (int j = 0; j < syn0[i].length; j++)
            {
                syn0[i][j] = (random.nextDouble() - 0.5) / layerSize;
            }
        }
    }

    public void saveBinaryModel(String path) throws IOException
    {
        logger.info("save binary model to path:{} ...", path);
        final long startTime = System.currentTimeMillis();
        try (
                DataOutputStream dataOutputStream = new DataOutputStream(
                        new BufferedOutputStream(Files.newOutputStream(Paths.get(path))))
        )
        {
            dataOutputStream.writeInt(idx2Word.size());
            dataOutputStream.writeInt(layerSize);
            double[] tempSyn0;
            for (int i = 0; i < idx2Word.size(); i++)
            {
                dataOutputStream.writeUTF(idx2Word.get(i).value);
                tempSyn0 = syn0[i];
                for (double d : tempSyn0)
                {
                    dataOutputStream.writeDouble(d);
                }
            }
        }
        logger.info("save model to path:{} success! take time:{}ms.", path, System.currentTimeMillis() - startTime);
    }

    protected void initNegative()
    {
        logger.info("init negative...");
        final long startTime = System.currentTimeMillis();
        int[] nodes = new int[idx2Word.size()];
        int[] count = new int[idx2Word.size()];
        for (int i = 0; i < idx2Word.size(); i++)
        {
            nodes[i] = i;
            count[i] = idx2Word.get(i).getCount();
        }
        this.negativeSampling = new AliasSampling(nodes, count);
        logger.info("init negative success! take time:{}ms.", System.currentTimeMillis() - startTime);
    }

    public int getLayerSize()
    {
        return layerSize;
    }

    public void setLayerSize(int layerSize)
    {
        this.layerSize = layerSize;
    }

    public int getWindow()
    {
        return window;
    }

    public void setWindow(int window)
    {
        this.window = window;
    }

    public double getSample()
    {
        return sample;
    }

    public void setSample(double sample)
    {
        this.sample = sample;
    }

    public double getAlpha()
    {
        return alpha;
    }

    public void setAlpha(double alpha)
    {
        this.alpha = alpha;
        this.startingAlpha = alpha;
    }

    public void setMAX_EXP(int MAX_EXP)
    {
        this.MAX_EXP = MAX_EXP;
    }

    public void setNegative(int negative)
    {
        this.negative = negative;
    }

}
