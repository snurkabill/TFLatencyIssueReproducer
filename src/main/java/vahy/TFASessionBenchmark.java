package vahy;


import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.RunnerException;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;


// TENSORFLOW 2
//import com.google.protobuf.InvalidProtocolBufferException;
//import org.tensorflow.proto.framework.GraphDef;
//import org.tensorflow.types.TFloat64;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;

@Fork(value = 3, jvmArgs = {"-Xms4G", "-Xmx4G"})
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class TFASessionBenchmark {

    public static void main(String[] args) throws IOException, RunnerException {
        org.openjdk.jmh.Main.main(args);
    }

//    private static final String PYTHON_PATH = System.getProperty("user.home") + "/.local/virtualenvs/tf_2_3/bin/python";
    private static final String PYTHON_PATH = path_to_python_with_TF_here;

    private Session session;

//    TENSORFLOW 2
//    private Tensor<TFloat64> tfInput;

//    TENSORFLOW 1
    private Tensor<?> tfInput;

    @Setup
    public void setUp() throws IOException, InterruptedException {
        SplittableRandom random = new SplittableRandom(0);

        var modelPath = Paths.get(TFASessionBenchmark.class.getClassLoader().getResource("tfModelPrototypes/IDENTITY.py").getPath());
        var modelRepresentation = TFHelper.loadTensorFlowModel(modelPath, PYTHON_PATH, random.nextLong(), 1, 1, 0);

        Graph commonGraph = new Graph();
        commonGraph.importGraphDef(modelRepresentation);
        session = new Session(commonGraph);
        session.runner().addTarget("init").run();

//        TENSORFLOW 2
//        tfInput = TFloat64.scalarOf(42.0);

//        TENSORFLOW 1
        tfInput = Tensor.create(42.0);
    }

    @TearDown
    public void tearDown() {
        tfInput.close();
        session.close();
    }

    @Benchmark
    public void sessionScalarIdentity() {
        List<Tensor<?>> output = session
            .runner()
            .feed("input_node", tfInput)
            .fetch("prediction_node", 0)
            .run();
        if (output.size() != 1) {
            for (Tensor<?> tensor : output) {
                tensor.close();
            }
            throw new IllegalStateException("There is expected only one output tensor in this scenario. If multiple tensors present on output, different method should be written to handle it. Got tensors: [" + output.size() + "]");
        }
        output.get(0).close();
    }

}
