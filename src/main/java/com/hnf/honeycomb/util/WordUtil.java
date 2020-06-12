package com.hnf.honeycomb.util;

import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.SegmentationAlgorithm;
import org.apdplat.word.segmentation.Word;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hnf
 */
public class WordUtil {

    /**
     * 返回所有分词方式的结果
     *
     * @param text
     * @return
     */
    public static Map<String, String> segMore(String text) {
        Map<String, String> map = new HashMap<>();
        for (SegmentationAlgorithm segmentationAlgorithm : SegmentationAlgorithm.values()) {
            map.put(segmentationAlgorithm.getDes(), seg(text, segmentationAlgorithm));
        }
        return map;
    }

    /**
     * 根据分词方式返回词结果
     *
     * @param text
     * @param segmentationAlgorithm
     * @return
     */
    public static String seg(String text, SegmentationAlgorithm segmentationAlgorithm) {
        StringBuilder result = new StringBuilder();
        for (Word word : WordSegmenter.segWithStopWords(text, segmentationAlgorithm)) {
            result.append(word.getText()).append(" ");
        }
        return result.toString();
    }

}
