# JCABench
A set of [JMH](https://github.com/openjdk/jmh) based benchmarks for the Java Cryptography Architecture (JCA) implementations.

## Execution
```
mvn clean compile
mvn exec:java -Dexec.mainClass=com.github.jcabench.BenchmarkExecutor -Dexec.args=<XXXBenchmarks>
```

## Test results
The test results are available at https://github.com/johnshajiang/JCABench/wiki
