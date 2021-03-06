package com.kanakis.resilient.perses;

import com.kanakis.resilient.perses.core.AttackProperties;
import com.kanakis.resilient.perses.testApp.TargetClass;
import com.kanakis.resilient.perses.core.AgentLoader;
import com.kanakis.resilient.perses.core.MBeanWrapper;
import com.sun.tools.attach.VirtualMachine;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.lang.management.ManagementFactory;

public class LatencyTest {
    private static MBeanWrapper mBeanWrapper;

    @BeforeClass
    public static void init() throws Exception {
        final VirtualMachine jvm = AgentLoader.run("", ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
        mBeanWrapper = MBeanWrapper.getMBean(jvm);
    }

    @AfterClass
    public static void tearDown() throws IOException {
        if(mBeanWrapper != null)
            mBeanWrapper.close();
    }

    @Test
    public void should_add_latency() {
        AttackProperties properties = new AttackProperties();
        properties.setClassPath("com.kanakis.resilient.perses.testApp.TargetClass");
        properties.setMethodName("targetMethod");
        properties.setLatency(1000);

        mBeanWrapper.addLatency(properties);
        final long time = timed(new TargetClass()::targetMethod);

        Assert.assertTrue(time >= 1000 && time < 2000);
    }

    @Test
    public void should_add_latency_when_called_with_defined_signature() {
        AttackProperties properties = new AttackProperties();
        properties.setClassPath("com.kanakis.resilient.perses.testApp.TargetClass");
        properties.setMethodName("targetMethod");
        properties.setSignature("()Z");
        properties.setLatency(1000);

        mBeanWrapper.addLatency(properties);
        final long time = timed(new TargetClass()::targetMethod);

        Assert.assertTrue(time >= 1000 && time < 2000);
    }

    public static long timed(Runnable runnable) {
        long startTime = System.currentTimeMillis();
        runnable.run();
        return System.currentTimeMillis() - startTime;
    }

}
