package me.mikusugar.node2vec;

/**
 * @description
 * @author mikusugar
 * @version 1.0, 2023/8/4 14:42
 */
public class NodeEntry implements Comparable<NodeEntry>
{
    private final int id;

    private final double score;

    public NodeEntry(int id, double score)
    {
        this.id = id;
        this.score = score;
    }

    public int getId()
    {
        return id;
    }

    public double getScore()
    {
        return score;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        NodeEntry nodeEntry = (NodeEntry)o;

        if (id != nodeEntry.id)
            return false;
        return Double.compare(score, nodeEntry.score) == 0;
    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        result = id;
        temp = Double.doubleToLongBits(score);
        result = 31 * result + (int)(temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public int compareTo(NodeEntry o)
    {
        if (this.score == o.score)
        {
            return Integer.compare(this.id, o.id);
        }
        return Double.compare(this.score, o.score);
    }

    @Override
    public String toString()
    {
        return "{" + "id=" + id + ", score=" + score + '}';
    }
}
