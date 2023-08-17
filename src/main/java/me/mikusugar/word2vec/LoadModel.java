package me.mikusugar.word2vec;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import me.mikusugar.common.VectorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * @description
 * @author mikusugar
 * @version 1.0, 2023/8/14 13:44
 */
public class LoadModel
{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Map<String, double[]> vecMap;

    private int topSize = 10;

    public void loadBinaryMode(String modelFilePath) throws IOException
    {
        logger.info("load binary model {}....", modelFilePath);
        final long startTime = System.currentTimeMillis();
        try (
                DataInputStream dis = new DataInputStream(
                        new BufferedInputStream(Files.newInputStream(Paths.get(modelFilePath))))
        )
        {
            final int wordSize = dis.readInt();
            final int layerSize = dis.readInt();
            this.vecMap = new HashMap<>(wordSize);
            for (int i = 0; i < wordSize; i++)
            {
                final String word = dis.readUTF();
                final double[] vec = new double[layerSize];
                for (int j = 0; j < layerSize; j++)
                {
                    vec[j] = dis.readDouble();
                }
                vecMap.put(word, vec);
            }
        }
        logger.info("load success! take time:{}ms.", System.currentTimeMillis() - startTime);
    }

    public List<WordEntry> closestWords(String word)
    {
        final double[] vector = vecMap.get(word);
        Preconditions.checkArgument(vector != null, "not found {}", word);
        final PriorityQueue<WordEntry> pq = new PriorityQueue<>();
        final int size = Math.min(vecMap.size() - 1, topSize);
        vecMap.forEach((curWord, curVec) ->
        {
            if (!curWord.equals(word))
            {
                final double similarityScore = VectorUtils.calculateCosineSimilarity(vector, curVec);
                pq.add(new WordEntry(curWord, similarityScore));
                if (pq.size() > size)
                {
                    pq.poll();
                }
            }
        });
        return pq2List(pq);
    }

    public List<WordEntry> closestWords(Collection<String> words)
    {
        Preconditions.checkNotNull(words);
        Preconditions.checkArgument(!words.isEmpty(), "words not empty!");

        final double[] sumVec = sumVec(words);
        final int size = Math.min(vecMap.size() - 1, topSize);

        final HashSet<String> uniqueWords = new HashSet<>(words);
        final PriorityQueue<WordEntry> pq = new PriorityQueue<>();
        vecMap.forEach((curWord, curVec) ->
        {
            if (!uniqueWords.contains(curWord))
            {
                final double similarityScore = VectorUtils.calculateCosineSimilarity(sumVec, curVec);
                pq.add(new WordEntry(curWord, similarityScore));
                if (pq.size() > size)
                {
                    pq.poll();
                }
            }
        });

        return pq2List(pq);
    }

    public List<WordEntry> analogy(String word1, String word2, String word3)
    {
        final double[] vec1 = vecMap.get(word1);
        Preconditions.checkNotNull(vec1, "not found {}!", word1);
        final double[] vec2 = vecMap.get(word2);
        Preconditions.checkNotNull(vec2, "not found {}!", word2);
        final double[] vec3 = vecMap.get(word3);
        Preconditions.checkNotNull(vec3, "not found {}!", word3);

        final int size = Math.min(vecMap.size() - 1, topSize);
        PriorityQueue<WordEntry> pq = new PriorityQueue<>();
        final Set<String> inputSet = Sets.newHashSet(word1, word2, word3);
        vecMap.forEach((curWord, curVec) ->
        {
            if (!inputSet.contains(curWord))
            {
                double score = VectorUtils.wordVectorAnalogyScore(vec1, vec2, vec3, curVec);
                pq.add(new WordEntry(curWord, score));
                if (pq.size() > size)
                {
                    pq.poll();
                }
            }
        });
        return pq2List(pq);

    }

    private double[] sumVec(Collection<String> words)
    {
        double[] res = null;
        for (String word : words)
        {
            final double[] vec = vecMap.get(word);
            Preconditions.checkNotNull(vec, "{} not found!", word);
            if (res == null)
            {
                res = Arrays.copyOf(vec, vec.length);
            }
            else
            {
                for (int i = 0; i < vec.length; i++)
                {
                    res[i] += vec[i];
                }
            }
        }
        return res;
    }

    private static <T> List<T> pq2List(PriorityQueue<T> pq)
    {
        final List<T> res = new ArrayList<>(pq.size());
        while (!pq.isEmpty())
        {
            res.add(pq.poll());
        }
        Collections.reverse(res);
        return res;
    }

    public Map<String, double[]> getVecMap()
    {
        return vecMap;
    }

    public int getTopSize()
    {
        return topSize;
    }

    public void setTopSize(int topSize)
    {
        this.topSize = topSize;
    }

    public static class WordEntry implements Comparable<WordEntry>
    {
        public final String value;

        public final double score;

        public WordEntry(String value, double score)
        {
            this.value = value;
            this.score = score;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            WordEntry wordEntry = (WordEntry)o;

            if (Double.compare(score, wordEntry.score) != 0)
                return false;
            return value.equals(wordEntry.value);
        }

        @Override
        public int hashCode()
        {
            int result;
            long temp;
            result = value.hashCode();
            temp = Double.doubleToLongBits(score);
            result = 31 * result + (int)(temp ^ (temp >>> 32));
            return result;
        }

        @Override
        public int compareTo(WordEntry o)
        {
            if (this.score == o.score)
            {
                return this.value.compareTo(o.value);
            }
            return Double.compare(this.score, o.score);
        }

        @Override
        public String toString()
        {
            return "{" + value + "," + score + "}";
        }
    }

}
