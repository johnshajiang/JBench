# JBench
A set of [JMH](https://github.com/openjdk/jmh) based benchmarks.

## Usages
```
# Compile the source
mvn clean compile

# Execute a benchmark class directly
mvn exec:java -Dexec.mainClass=com.github.jbench.<XXXBenchmarks>

# Execute benchmark(s) via BenchmarkExecutor
mvn exec:java -Dexec.mainClass=com.github.jbench.BenchmarkExecutor -Dexec.args=<regex>
```

## Test results
The test results are available at https://github.com/johnshajiang/JBench/wiki
