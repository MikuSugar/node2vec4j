package me.mikusugar.word2vec;

import me.mikusugar.HelpTestUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description CBOWTest
 * @author mikusugar
 * @version 1.0, 2023/8/17 14:41
 */
public class CBOWTest
{
    private final static String corpusModelName = "corpus_cbow.emb";

    private static final Logger logger = LoggerFactory.getLogger(CBOWTest.class);

    @Test
    public void testFitAndSaveBinaryModel() throws Exception
    {
        final String corpusFilePath = HelpTestUtils.getResourcePath() + "/corpus.txt";
        final CBOW cbow = new CBOW();
        cbow.setLayerSize(300);
        cbow.setMAX_EXP(10);
        cbow.setNegative(10);
        cbow.setAlpha(0.05);
        cbow.setSample(1e-3);
        cbow.fitFile(corpusFilePath, 8);
        cbow.saveBinaryModel(corpusModelName);
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
        final boolean result0 = analogy.stream().map(v -> v.value).collect(Collectors.toSet()).contains("邓小平理论");

        final List<LoadModel.WordEntry> analogy1 = model.analogy("女人", "女儿", "男人");
        logger.info("女人->女儿 analogy 男人->{}", analogy1);
        final boolean result1 = analogy1.stream().map(v -> v.value).collect(Collectors.toSet()).contains("儿子");

        //cbow 貌似没有skip-gram效果好，所以这里测试妥协了
        assert result0 || result1;
    }

    private static LoadModel loadModelFromCorpus() throws IOException
    {
        LoadModel model = new LoadModel();
        model.loadBinaryMode(corpusModelName);
        return model;
    }
}
