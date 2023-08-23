package me.mikusugar.word2vec;

import it.unimi.dsi.fastutil.ints.IntList;

import java.util.concurrent.ThreadLocalRandom;

public class SkipGram extends Word2Vec
{

    public SkipGram()
    {

    }

    @Override
    protected void tranLine(String line)
    {
        String[] strs = line.split("[\\s　]+");
        wordCount.add(strs.length);
        final IntList sentence = getSentence(strs);

        for (int index = 0; index < sentence.size(); index++)
        {
            skipGram(index, sentence, ThreadLocalRandom.current().nextInt(window));
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
            final double g = getGradient(syn0[input], syn1[target], label);
            for (int i = 0; i < layerSize; i++)
            {
                neu1e[i] += g * syn1[target][i];
                syn1[target][i] += g * syn0[input][i];
            }
        }
    }

}
