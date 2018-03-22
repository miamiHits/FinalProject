package FinalProject.DAL;

import FinalProject.BL.DataCollection.AlgorithmProblemResult;

import java.io.IOException;
import java.util.List;


public interface FileSaverInterface {

    void saveExpirmentResult(List<AlgorithmProblemResult> problemResults) throws IOException;
}