package FinalProject.BL;

import FinalProject.BL.DataCollection.AlgorithmProblemResult;

public interface ExperimentInterface {
    void runExperiment();

    // gal: this one should be invoked by the data collection agent notifying all data
    // resulted from the algorithm-problem configuration run was fully processed
    // IMPORTANT - operation is non-blocking
    void algorithmProblemComboRunEnded(AlgorithmProblemResult result);

    void stopExperiment();
}
