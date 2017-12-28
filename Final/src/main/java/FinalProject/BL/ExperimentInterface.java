package FinalProject.BL;

import FinalProject.BL.DataCollection.AlgorithmProblemResult;

public interface ExperimentInterface {
    void runExperiment();

    // gal: this one should be invoked by the data collection agent notifying all data
    // resulted from the algorithm-problem configuration run was fully processed
    // IMPORTANT - the method is blocking and should be invoked when the data collector has done all that is needed for the current configuration
    void algorithmRunEnded(AlgorithmProblemResult result);

    void stopExperiment();

    //TODO gal consider removing this one
    boolean experimentCompleted();
}
