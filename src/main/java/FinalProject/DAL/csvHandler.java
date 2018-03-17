package FinalProject.DAL;

import FinalProject.BL.DataCollection.AlgorithmProblemResult;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class csvHandler implements FileSaverInterface {

    private String filePathToSave;

    public csvHandler (String filePath)
    {
        this.filePathToSave = filePath;
    }

    @Override
    public void saveExpirmentResult(List<AlgorithmProblemResult> problemResults) throws IOException {
        FileWriter writer = new FileWriter(this.filePathToSave, true);

        for (AlgorithmProblemResult prob: problemResults)
        {
            writer.write("Problem Name: " + prob.getProblem());
            writer.write("\n");
            writer.write("Algorithm Name: " + prob.getAlgorithm());
            writer.write("\n");
            writer.write("\n");
            writer.write("\n");

            writer.write("Iteration number,");
            writer.write("Average Price,");
            writer.write("Total Grade");
            Double[] average = prob.getAvgPricePerIteration().values().toArray(new Double[prob.getAvgPricePerIteration().size()]);
            Double[] total = prob.getTotalGradePerIteration().values().toArray(new Double[prob.getTotalGradePerIteration().size()]);
           // Double[] bestAgent = prob..values().toArray(new Double[prob.getTotalGradePerIteration().size()]);
            List<String> toPrint = new ArrayList<>();
            for (int i=0; i<average.length; i++)
            {
                toPrint.clear();
                toPrint.add(String.valueOf(i));
                //writer.write("DEBUG YARDEN: " +average[i].toString());
                toPrint.add(average[i].toString());
               // writer.write("DEBUG YARDEN: " +total[i].toString());
                toPrint.add(total[i].toString());
                CSVUtils.writeLine(writer,toPrint);
            }
        }

        writer.flush();
        writer.close();
    }
}
