/**
 * Created by Eugenio on 11/4/16.
 */

import it.unimi.dsi.webgraph.*;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.ImmutableGraph;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;



public class HarmonicCentrality {
    private final ImmutableGraph graph;
    private final int numberOfThreads;
    public final double[] harmonic;

    public HarmonicCentrality(ImmutableGraph graph, int requestedThreads) {
        this.graph = graph;
        this.harmonic = new double[graph.numNodes()];
        this.numberOfThreads = requestedThreads != 0?requestedThreads:Runtime.getRuntime().availableProcessors();
    }

    public static void main(String[] args) throws IOException, InterruptedException, JSAPException {
        CharSequence basename = "/home/eugenio/Downloads/Graphs/cnr2000/cnr-2000-hc";
        if (args.length > 0) {
            try {

                ImmutableGraph graph = ImmutableGraph.loadOffline(basename);


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else {
            System.out.println("Error: no input graph provided");
        }
    }
}
