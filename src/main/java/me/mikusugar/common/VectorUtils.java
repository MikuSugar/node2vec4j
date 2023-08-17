package me.mikusugar.common;

import java.util.stream.IntStream;

/**
 * @description
 * @author mikusugar
 * @version 1.0, 2023/8/14 14:08
 */
public class VectorUtils
{
    /**
     * Calculates the cosine similarity between two vectors.
     *
     * @param vectorA the first vector
     * @param vectorB the second vector
     * @return the cosine similarity between the two vectors
     * @throws IllegalArgumentException if the vectors have different dimensions or one or both vectors have zero norm
     */
    public static double calculateCosineSimilarity(double[] vectorA, double[] vectorB)
    {
        if (vectorA.length != vectorB.length)
        {
            throw new IllegalArgumentException("Vectors must have the same dimensions");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++)
        {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        if (normA == 0.0 || normB == 0.0)
        {
            throw new IllegalArgumentException("One or both vectors have zero norm");
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public static double[] subtractVectors(double[] v1, double[] v2)
    {
        return IntStream.range(0, v1.length).mapToDouble(i -> v1[i] - v2[i]).toArray();
    }

    /**
     * Calculates the analogy score between word vectors.
     *
     * @param vA the first word vector
     * @param vB the second word vector
     * @param vC the third word vector
     * @param vD the fourth word vector
     * @return the analogy score
     * @throws IllegalArgumentException if the vectors have different dimensions or one or both vectors have zero norm
     */
    public static double wordVectorAnalogyScore(double[] vA, double[] vB, double[] vC, double[] vD)
    {
        return calculateCosineSimilarity(subtractVectors(vA, vB), subtractVectors(vC, vD));
    }
}
