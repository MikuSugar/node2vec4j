package me.mikusugar.word2vec;

/**
 * @description
 * @author mikusugar
 * @version 1.0, 2023/8/10 10:22
 */
public class Word
{
    public final String value;

    private int count;

    public Word(String value)
    {
        this.value = value;
        this.count = 1;
    }

    public int getCount()
    {
        return count;
    }

    public void incrementCount()
    {
        this.count++;
    }
}
