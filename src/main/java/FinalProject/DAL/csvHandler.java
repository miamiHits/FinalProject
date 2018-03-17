package FinalProject.DAL;

import FinalProject.BL.DataCollection.AlgorithmProblemResult;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class csvHandler implements FileSaverInterface {

    private String filePathToSave;
    public csvHandler (String filePath)
    {
        this.filePathToSave = filePath;
    }

    @Override
    public void saveExpirmentResult(List<AlgorithmProblemResult> problemResults) throws IOException {
        FileWriter writer = new FileWriter(this.filePathToSave);
        for (AlgorithmProblemResult prob: problemResults)
        {
            for (Map.Entry<Integer, Double> entry : prob.getAvgPricePerIteration().entrySet()) {
                CSVUtils.writeLine(writer, Arrays.asList("a", "b", "c", "d"));
            }
        }

        writer.flush();
        writer.close();


    }
}
