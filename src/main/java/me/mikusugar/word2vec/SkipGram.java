package me.mikusugar.word2vec;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.IntList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SkipGram extends Word2Vec
{
    private static final Logger logger = LoggerFactory.getLogger(SkipGram.class);

    public SkipGram()
    {

    }

    private void tranLine(String line)
    {
        String[] strs = line.split("[\\s　]+");
        wordCount.addAndGet(strs.length);
        final IntList sentence = getSentence(strs);

        for (int index = 0; index < sentence.size(); index++)
        {
            skipGram(index, sentence, random.nextInt(window));
        }
    }

    private void trainModel(File file, int threads) throws IOException, InterruptedException
    {
        final long startTime = System.currentTimeMillis();
        this.alpha = startingAlpha;
        // @formatter:off
        ThreadPoolExecutor executor = new ThreadPoolExecutor(threads, threads, 5, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(threads * 3), new ThreadPoolExecutor.CallerRunsPolicy());
        // @formatter:on

        try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()))))
        {
            wordCount.set(0);
            lastWordCount = 0;
            wordCountActual = 0;
            while (br.ready())
            {
                updateLearRate();
                final String line = br.readLine();
                executor.execute(() -> tranLine(line));
            }
            // 关闭线程池
            executor.shutdown();

            // 等待所有任务执行完成
            //noinspection ResultOfMethodCallIgnored
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            logger.info("Vocab size: " + word2idx.size());
            logger.info("Words in train file: " + trainWordsCount);
            logger.info("success train over! take time:{}ms.", System.currentTimeMillis() - startTime);
        }
    }

    /**
     * skipGram
     *
     * @param index    the index of the word in the sentence
     * @param sentence the sentence containing the word
     * @param b        the context window size
     */
    private void skipGram(int index, IntList sentence, int b)
    {
        int input = sentence.getInt(index);
        int a, c;
        for (a = b; a < window * 2 + 1 - b; a++)
        {
            if (a == window)
            {
                continue;
            }
            c = index - window + a;
            if (c < 0 || c >= sentence.size())
            {
                continue;
            }

            double[] neu1e = new double[layerSize];// 误差项
            int we = sentence.getInt(c);

            negativeSampling(input, we, neu1e);

            // Learn weights input -> hidden
            for (int j = 0; j < layerSize; j++)
            {
                syn0[input][j] += neu1e[j];
            }
        }

    }

    private void negativeSampling(int input, int we, double[] neu1e)
    {
        int target;
        int label;
        for (int d = 0; d < negative + 1; d++)
        {
            if (d == 0)
            {
                target = we;
                label = 1;
            }
            else
            {
                target = negativeSampling.next();
                if (target == input)
                {
                    continue;
                }
                label = 0;
            }
            final double g = getGradient(input, target, label);
            for (int i = 0; i < layerSize; i++)
            {
                neu1e[i] += g * syn1[target][i];
                syn1[target][i] += g * syn0[input][i];
            }
        }
    }

    @Override
    public void fitFile(String filePath, int threads) throws Exception
    {
        File file = new File(filePath);
        Preconditions.checkArgument(file.isFile());
        Preconditions.checkArgument(threads >= 1);

        createExpTable();
        readVocab(file);
        initNet();
        initNegative();
        trainModel(file, threads);
    }

}
