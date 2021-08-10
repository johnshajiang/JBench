package com.github.jcabench;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static com.github.jcabench.BenchmarkUtils.formattedTime;

/**
 * Execute the specified benchmark(s).
 */
public class BenchmarkExecutor {

    private static final String JMH_EXT = ".jmh";

    private final ChainedOptionsBuilder optionsBuilder;

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
        String className = benchmarkClass.getName();
        String filename = className + "-" + formattedTime() + JMH_EXT;
        return execute(className, filename);
    }

    public static void main(String[] args) throws RunnerException {
        String filename = "Benchmarks-" + formattedTime() + JMH_EXT;
        String regrex = args != null && args.length > 0 ? args[0] : "Benchmarks";
        new BenchmarkExecutor().execute(regrex, filename);
    }
}
