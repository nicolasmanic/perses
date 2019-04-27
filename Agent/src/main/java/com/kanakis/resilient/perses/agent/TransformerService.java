package com.kanakis.resilient.perses.agent;


import java.lang.instrument.Instrumentation;

public class TransformerService implements TransformerServiceMBean {

    /**
     * The JVM's instrumentation instance
     */
    private final Instrumentation instrumentation;

    TransformerService(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    @Override
    public void throwException(String className, String methodName) {
        TransformProperties properties = new TransformPropertiesBuilder(methodName, new FaultMode())
                .createTransformProperties();
        transform(className, properties);
    }

    @Override
    public void addLatency(String className, String methodName, long latency) {
        TransformProperties properties = new TransformPropertiesBuilder(methodName, new LatencyMode())
                .setLatency(latency)
                .createTransformProperties();
        transform(className, properties);
    }

    @Override
    public void restoreMethod(String className, String methodName) {
        TransformProperties properties =  new TransformPropertiesBuilder(methodName, new RestoreMode())
                .createTransformProperties();
        transform(className, properties);
    }

    /**
     * Registers a transformer and executes the transformation
     *
     * @param className The binary name of the target class
     */
    private void transform(String className, TransformProperties properties) {
        Class<?> clazz = locateClass(className);
        ClassLoader classLoader = clazz.getClassLoader();
        ChaosTransformer dt = new ChaosTransformer(classLoader, clazz.getName(), properties);
        instrumentation.addTransformer(dt, true);
        try {
            instrumentation.retransformClasses(clazz);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to transform [" + clazz.getName() + "]", ex);
        } finally {
            instrumentation.removeTransformer(dt);
        }
    }

    /**
     * Locate target class to be transformed
     *
     * @param className The binary name of the target class
     */
    private Class<?> locateClass(String className) {
        Class<?> targetClazz = null;
        try {
            targetClazz = Class.forName(className);
            return targetClazz;
        } catch (Exception ex) { /* Do nothing */ }
        // In case the class cannot be located by name search all
        for (Class<?> clazz : instrumentation.getAllLoadedClasses()) {
            if (clazz.getName().equals(className)) {
                targetClazz = clazz;
                return targetClazz;
            }
        }
        throw new RuntimeException("Failed to locate class [" + className + "]");
    }
}