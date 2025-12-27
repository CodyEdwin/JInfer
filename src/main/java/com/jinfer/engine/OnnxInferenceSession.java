package com.jinfer.engine;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * ONNX Runtime based inference session using DJL.
 */
public class OnnxInferenceSession implements InferenceSession {
    
    private static final Logger logger = LoggerFactory.getLogger(OnnxInferenceSession.class);
    
    private ZooModel<long[], float[]> model;
    private Predictor<long[], float[]> predictor;
    private NDManager manager;
    private final int vocabSize;
    private final int maxContextLength;

    public OnnxInferenceSession(Path modelPath, int vocabSize, int maxContextLength) 
            throws ModelNotFoundException, MalformedModelException, IOException {
        this.vocabSize = vocabSize;
        this.maxContextLength = maxContextLength;
        this.manager = NDManager.newBaseManager();
        
        logger.info("Loading ONNX model from: {}", modelPath);
        
        Criteria<long[], float[]> criteria = Criteria.builder()
                .setTypes(long[].class, float[].class)
                .optModelPath(modelPath)
                .optEngine("OnnxRuntime")
                .optTranslator(new LLMTranslator())
                .build();
        
        this.model = criteria.loadModel();
        this.predictor = model.newPredictor();
        
        logger.info("ONNX model loaded successfully");
    }

    @Override
    public float[] forward(long[] inputIds, long[] attentionMask) {
        try {
            return predictor.predict(inputIds);
        } catch (TranslateException e) {
            logger.error("Inference failed", e);
            throw new RuntimeException("Inference failed", e);
        }
    }

    @Override
    public int getVocabSize() {
        return vocabSize;
    }

    @Override
    public int getMaxContextLength() {
        return maxContextLength;
    }

    @Override
    public void close() {
        if (predictor != null) {
            predictor.close();
        }
        if (model != null) {
            model.close();
        }
        if (manager != null) {
            manager.close();
        }
    }

    /**
     * Translator for LLM inference.
     */
    private class LLMTranslator implements Translator<long[], float[]> {
        
        @Override
        public NDList processInput(TranslatorContext ctx, long[] input) {
            NDManager mgr = ctx.getNDManager();
            NDArray inputIds = mgr.create(input).reshape(1, input.length);
            NDArray attentionMask = mgr.ones(new Shape(1, input.length), DataType.INT64);
            return new NDList(inputIds, attentionMask);
        }

        @Override
        public float[] processOutput(TranslatorContext ctx, NDList list) {
            // Get the last token's logits
            NDArray logits = list.singletonOrThrow();
            
            // Shape is typically [batch, seq_len, vocab_size]
            // We want logits for the last position
            long[] shape = logits.getShape().getShape();
            if (shape.length == 3) {
                logits = logits.get(0).get(shape[1] - 1);
            } else if (shape.length == 2) {
                logits = logits.get(0);
            }
            
            return logits.toFloatArray();
        }
    }
}
