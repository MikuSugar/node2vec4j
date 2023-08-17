package me.mikusugar.word2vec;

import me.mikusugar.HelpTestUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * @description
 * @author mikusugar
 * @version 1.0, 2023/8/10 15:31
 */
public class SKipGramTest
{
    private final static String corpusModelName = "corpus.emb";

    @Test
    public void fitCorpus() throws Exception
    {
        final String corpusFilePath = HelpTestUtils.getResourcePath() + "/corpus.txt";
        final SkipGram skipGram = new SkipGram();
        skipGram.setLayerSize(300);
        skipGram.setMAX_EXP(10);
        skipGram.setNegative(10);
        skipGram.fitFile(corpusFilePath, 8);
        skipGram.saveBinaryModel(corpusModelName);
    }

    @Test
    public void ficCorpusResult() throws IOException
    {
        LoadModel model = new LoadModel();
        model.loadBinaryMode(corpusModelName);
        System.out.println(model.closestWords("中国"));
        System.out.println("邓小平：" + model.closestWords("邓小平"));
        System.out.println("魔术队:" + model.closestWords("魔术队"));
        System.out.println("过年：" + model.closestWords("过年"));
        System.out.println("香港" + " 澳门：" + model.closestWords(Arrays.asList("香港", "澳门")));
        System.out.println("###########################");
        System.out.println(model.analogy("毛泽东", "邓小平", "毛泽东思想"));
        System.out.println("###########################");
        System.out.println(model.analogy("女人", "女儿", "男人"));
        System.out.println("###########################");
        System.out.println(model.analogy("北京", "中国", "巴黎"));
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~");
    }
}
