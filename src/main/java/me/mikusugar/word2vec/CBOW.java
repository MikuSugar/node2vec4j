package me.mikusugar.word2vec;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.IntList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @description CBOW
 * @author mikusugar
 * @version 1.0, 2023/8/17 13:57
 */
public class CBOW extends Word2Vec
{
    private static final Logger logger = LoggerFactory.getLogger(CBOW.class);

    @Override
    public void fitFile(String filePath, int threads) throws Exception
    {
        File file = new File(filePath);
        validateInputs(threads, file);

        createExpTable();
        readVocab(file);
        initNet();
        initNegative();
        trainModel(file, threads);

    }

    private void trainModel(File file, int threads) throws IOException
    {
        //TODO 多线程支持
        Preconditions.checkArgument(threads == 1, "Does not support multithreading.");
        final long startTime = System.currentTimeMillis();
        this.alpha = startingAlpha;

        try (BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            wordCount.set(0);
            lastWordCount = 0;
            wordCountActual = 0;
            while (br.ready())
            {
                updateLearRate();
                final String line = br.readLine();
                tranLine(line);
            }
            logger.info("Vocab size: " + word2idx.size());
            logger.info("Words in train file: " + trainWordsCount);
            logger.info("success train over! take time:{}ms.", System.currentTimeMillis() - startTime);

        }

    }

    private void tranLine(String line)
    {
        String[] strs = line.split("[\\s　]+");
        wordCount.addAndGet(strs.length);
        final IntList sentence = getSentence(strs);
        for (int inputWordIdx = 0; inputWordIdx < sentence.size(); inputWordIdx++)
        {
            cbow(inputWordIdx, sentence, random.nextInt(window));
        }
    }

    public boolean checkNaN(double[] v)
    {
        for (double d : v)
        {
            if (Double.isNaN(d))
            {
                return true;
            }
        }
        return false;
    }

    private void cbow(int inputWordIdx, IntList sentence, int b)
    {
        final double[] hiddenVec = new double[layerSize];
        final double[] neu1e = new double[layerSize];
        int numWindow = 0;
        for (int a = b; a < window * 2 + 1 - b; a++)
        {
            final int c = getC(a, inputWordIdx, sentence);
            if (c == -1)
            {
                continue;
            }

            final int cWord = sentence.getInt(c);
            for (int i = 0; i < layerSize; i++)
            {
                hiddenVec[i] += syn0[cWord][i];
            }
            numWindow++;
        }
        if (numWindow != 0)
        {
            for (int i = 0; i < layerSize; i++)
            {
                hiddenVec[i] /= numWindow;
            }
        }

        final int inputWord = sentence.getInt(inputWordIdx);

        negativeSampling(inputWord, hiddenVec, neu1e);

        for (int a = b; a < window * 2 + 1 - b; a++)
        {
            final int c = getC(a, inputWordIdx, sentence);
            if (c == -1)
            {
                continue;
            }

            final int cWord = sentence.getInt(c);
            for (int i = 0; i < layerSize; i++)
            {
                syn0[cWord][i] += neu1e[i];
            }
        }
    }

    private void negativeSampling(int inputWord, double[] hiddenVec, double[] neu1e)
    {
        for (int d = 0; d <= negative; d++)
        {
            int label;
            int targetWord;
            if (d == 0)
            {
                label = 1;
                targetWord = inputWord;
            }
            else
            {
                label = 0;
                targetWord = negativeSampling.next();
                if (targetWord == inputWord)
                {
                    continue;
                }
            }
            double gradient = getGradient(hiddenVec, syn1[targetWord], label);
            for (int i = 0; i < layerSize; i++)
            {
                neu1e[i] += gradient * syn1[targetWord][i];
                syn1[targetWord][i] += gradient * hiddenVec[i];
            }
        }
    }

    private int getC(int a, int inputWordIdx, IntList sentence)
    {
        if (a == window)
        {
            return -1;
        }
        int c = inputWordIdx - window + a;
        if (c < 0 || c >= sentence.size())
        {
            return -1;
        }
        return c;
    }
}
