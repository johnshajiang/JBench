# JCABench
A set of [JMH](https://github.com/openjdk/jmh) based benchmarks for the Java Cryptography Architecture (`JCA`) implementations.

## Usages
```
# Compile the source
mvn clean compile

# Execute a benchmark class directly
mvn exec:java -Dexec.mainClass=com.github.jcabench.<XXXBenchmarks>

# Execute benchmark(s) via BenchmarkExecutor
mvn exec:java -Dexec.mainClass=com.github.jcabench.BenchmarkExecutor -Dexec.args=<regex>
```

## Test results
The test results are available at https://github.com/johnshajiang/JCABench/wiki
