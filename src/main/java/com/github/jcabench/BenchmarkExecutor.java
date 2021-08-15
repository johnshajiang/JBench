package com.github.jcabench;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static com.github.jcabench.BenchmarkUtils.formattedTime;

/**
 * Execute the specified benchmark(s).
 */
public class BenchmarkExecutor {

    private static final String JMH_EXT = ".jmh";

    private final ChainedOptionsBuilder optionsBuilder;

    static {
        setJavaClasspath();
    }

    public BenchmarkExecutor(OptionsBuilder optionsBuilder) {
        this.optionsBuilder = optionsBuilder;
    }

    public BenchmarkExecutor() {
        optionsBuilder = new OptionsBuilder()
                .warmupIterations(5)
                .measurementIterations(5)
                .mode(Mode.Throughput)
                .forks(2)
                .threads(1)
                .shouldDoGC(true)
                .shouldFailOnError(true)
                .resultFormat(ResultFormatType.TEXT)
                .shouldFailOnError(true)
                .timeUnit(TimeUnit.SECONDS)
                .jvmArgs("-server", "-Xms1024M", "-Xmx1024M");
    }

    public Collection<RunResult> execute(String pattern, String filename)
            throws RunnerException {
        Options options = optionsBuilder
                .include(pattern)
                .result(filename)
                .build();
        return new Runner(options).run();
    }

    public Collection<RunResult> execute(Class<?> benchmarkClass)
            throws RunnerException {
        return execute(benchmarkClass.getSimpleName(), filename(benchmarkClass));
    }

    public static void main(String[] args) throws RunnerException {
        if (args != null && args.length > 0) {
            new BenchmarkExecutor().execute(args[0],
                    filename(BenchmarkExecutor.class));
        } else {
            new BenchmarkExecutor().execute(AesBenchmarks.class);
            new BenchmarkExecutor().execute(ChaCha20Benchmarks.class);
            new BenchmarkExecutor().execute(DigestBenchmarks.class);
            new BenchmarkExecutor().execute(KeyPairGenBenchmarks.class);
            new BenchmarkExecutor().execute(MacBenchmarks.class);
        }
    }

    private static String filename(Class<?> clazz) {
        return clazz.getName() + "-" + formattedTime() + JMH_EXT;
    }

    // A workaround for executing JMH via Maven.
    // The command looks like the below:
    // mvn exec:java -Dexec.mainClass=com.github.jcabench.BenchmarkExecutor -Dexec.args=<regex>
    private static void setJavaClasspath() {
        URLClassLoader classLoader
                = (URLClassLoader) BenchmarkExecutor.class.getClassLoader();
        StringBuilder classpath = new StringBuilder();
        for(URL url : classLoader.getURLs()) {
            classpath.append(url.getPath()).append(File.pathSeparator);
        }
        System.setProperty("java.class.path", classpath.toString());
    }
}
