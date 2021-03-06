import com.martiansoftware.jsap.*;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.ArcListASCIIGraph;
import it.unimi.dsi.webgraph.ArrayListMutableGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.Transform;
import it.unimi.dsi.webgraph.algo.ConnectedComponents;
import it.unimi.dsi.webgraph.algo.StronglyConnectedComponents;
import org.apache.commons.lang.ArrayUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Wrote by Eugenio on 3/24/17.
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws JSAPException, IOException {
        SimpleJSAP jsap = new SimpleJSAP(Main.class.getName(), "Computes positive centralities of a graph using multiple parallel breadth-first visits.\n\nPlease note that to compute negative centralities on directed graphs (which is usually what you want) you have to compute positive centralities on the transpose.",
                new Parameter[]{
                        new Switch("expand", 'e', "expand", "Expand the graph to increase speed (no compression)."),
                        new Switch("mapped", 'm', "mapped", "Use loadMapped() to load the graph."),
                        new Switch("text", 't', "text", "Load a graph from a arc list txt file."),
                        new FlaggedOption("threads", JSAP.INTSIZE_PARSER, "0", false, 'T', "threads", "The number of threads to be used. If 0, the number will be estimated automatically."),
                        new UnflaggedOption("graphBasename", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, true, false, "The basename of the graph."),
                });

        JSAPResult jsapResult = jsap.parse(args);
        if(jsap.messagePrinted()) {
            System.exit(1);
        }

        String graphName = jsapResult.getString("graphBasename");
        String graphBasename = "./Graphs/" + graphName + "/" + graphName;
        int numberOfThreads = jsapResult.getInt("threads");

        ProgressLogger progressLogger = new ProgressLogger(LOGGER, "nodes");
        progressLogger.displayFreeMemory = true;
        progressLogger.displayLocalSpeed = true;

        ImmutableGraph graph;
        if (!jsapResult.getBoolean("text", false)) {
            graph = (new GraphReader(graphBasename, jsapResult.getBoolean("mapped", false), jsapResult.userSpecified("expand"), progressLogger)).getGraph();
        }
        else {
            graph = ArcListASCIIGraph.load(graphBasename+".txt", null);
        }

        graph = Transform.symmetrize(graph);
        System.out.println("Number of SCCs = " + StronglyConnectedComponents.compute(graph, false, null).numberOfComponents);
        int rounds = 2;
        System.out.println("Nodes = " + graph.numNodes());
        String resultPath = "./results/" + graphName + "_chechik" + ".json";
        JSONObject obj = new JSONObject();
        final int[] topk = new int[101];
        int[] totalBFS = new int[topk.length];
        int[] bfsApx = new int[topk.length];

        topk[0] = 1;
        for (int i = 1; i < topk.length; ++i) {
            topk[i] = 20 * i;
        }
        for (int i = 0; i < rounds; ++i) {
            int count = 0;

            for (int k : topk) {
                ChechikTopCloseness topCloseness = new ChechikTopCloseness(graph, progressLogger, numberOfThreads, k);
                try {
                    topCloseness.compute();
//                    GTLoader loader = new GTLoader(graphName, graph.numNodes());
//                    try {
//                        loader.load();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                   // Integer[] computed_top_k = topCloseness.getTopk();
                //    int[] exact = loader.getTopKNodes(k);

                  //  Set<Integer> computed_set = new HashSet<>(Arrays.asList(computed_top_k));
                   // Set<Integer> exact_set = new HashSet<>(Arrays.asList(ArrayUtils.toObject(exact)));
                   // double size = computed_set.size();
                   // computed_set.retainAll(exact_set);

                    //double precision = (double) computed_set.size() / k;

                    System.out.println("K = " + k);
                    //System.out.println("Precision = " + precision);
                    //System.out.println("Computed set size = " + (int) size + " exact top k size = " + exact_set.size());
                    System.out.println("Number of BFS = " + topCloseness.getNumberOfBFS());
                    System.out.println("BFS for approximation = " + topCloseness.getNumberOfBFSForApx() + "\nRemaining BFS = " + (topCloseness.getNumberOfBFS() - topCloseness.getNumberOfBFSForApx()));
                    totalBFS[count]+=topCloseness.getNumberOfBFS();
                    bfsApx[count]+=topCloseness.getNumberOfBFSForApx();
                    ++count;

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        for (int i = 0; i < topk.length; ++i){
            double[] val = {(double)totalBFS[i] / (double)rounds, (double)bfsApx[i] / (double)rounds};
            obj.put(String.valueOf(topk[i]), val);
        }

        try (FileWriter file = new FileWriter(resultPath)) {
            obj.write(file);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
