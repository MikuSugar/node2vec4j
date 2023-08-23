package me.mikusugar.word2vec;

import me.mikusugar.HelpTestUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description
 * @author mikusugar
 * @version 1.0, 2023/8/10 15:31
 */
public class SKipGramTest
{
    private final static String corpusModelName = "corpus.emb";

    private static final Logger logger = LoggerFactory.getLogger(SKipGramTest.class);

    @Test
    public void testFitAndSaveBinaryModel() throws Exception
    {
        final String corpusFilePath = HelpTestUtils.getResourcePath() + "/corpus.txt";
        final SkipGram skipGram = new SkipGram();
        skipGram.setLayerSize(300);
        skipGram.setMAX_EXP(10);
        skipGram.setNegative(10);
        skipGram.fitFile(corpusFilePath, Math.max(HelpTestUtils.getCPUCores(), 1));
        skipGram.saveBinaryModel(corpusModelName);
    }

    @Test
    public void testClosesWords() throws IOException
    {
        final LoadModel model = loadModelFromCorpus();
        final List<LoadModel.WordEntry> result = model.closestWords("邓小平");
        logger.info("邓小平 closesWords：" + result);
        assert result.stream().map(v -> v.value).collect(Collectors.toSet()).contains("毛泽东");
    }

    @Test
    public void testAnalogy() throws IOException
    {
        final LoadModel model = loadModelFromCorpus();
        final List<LoadModel.WordEntry> analogy = model.analogy("毛泽东", "毛泽东思想", "邓小平");
        logger.info("毛泽东->毛泽东思想 analogy 邓小平->{}", analogy);
        assert analogy.stream().map(v -> v.value).collect(Collectors.toSet()).contains("邓小平理论");

        final List<LoadModel.WordEntry> analogy1 = model.analogy("女人", "女儿", "男人");
        logger.info("女人->女儿 analogy 男人->{}", analogy1);
        assert analogy1.stream().map(v -> v.value).collect(Collectors.toSet()).contains("儿子");
    }

    private static LoadModel loadModelFromCorpus() throws IOException
    {
        LoadModel model = new LoadModel();
        model.loadBinaryMode(corpusModelName);
        return model;
    }
}
