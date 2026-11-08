import experiments.ExperimentRunner;

public class Main {
    public static void main(String[] args) throws Exception {
        String csv = args.length > 0 ? args[0] : "results/results.csv";
        ExperimentRunner.runAll(csv);
        System.out.println("Done. Results written to " + csv);
    }
}
