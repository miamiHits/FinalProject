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
            writer.write("Total Grade,");
            writer.write("Lowest Agent Price,");
            writer.write("Lowest Agent Name,");
            writer.write("Highest Agent,");
            writer.write("Highest Agent Name,");
            writer.write("\n");
            Double[] average = prob.getAvgPricePerIteration().values().toArray(new Double[prob.getAvgPricePerIteration().size()]);
           // writer.write("DEBUG: AVE " + prob.getAvgPricePerIteration().size() +",");
            Double[] total = prob.getTotalGradePerIteration().values().toArray(new Double[prob.getTotalGradePerIteration().size()]);
            //writer.write("DEBUG: total " + prob.getTotalGradePerIteration().size() +",");
            Double[] lowestAgentPrice = prob.getLowestCostForAgentInBestIteration().values().toArray(new Double[prob.getLowestCostForAgentInBestIteration().size()]);
            //writer.write("DEBUG: lowestAgentPrice " + prob.getLowestCostForAgentInBestIteration().size() +",");
            String[] lowestAgentName = prob.getLowestCostForAgentInBestIterationAgentName().values().toArray(new String[prob.getLowestCostForAgentInBestIterationAgentName().size()]);
            //writer.write("DEBUG: lowestAgentName " + prob.getLowestCostForAgentInBestIterationAgentName().size() +",");
            Double[] highestAgentPrice = prob.getHighestCostForAgentInBestIteration().values().toArray(new Double[prob.getHighestCostForAgentInBestIteration().size()]);
            //writer.write("DEBUG: highestAgentPrice " + prob.getHighestCostForAgentInBestIteration().size() +",");
            String[] highestAgentNames = prob.getHighestCostForAgentInBestIterationAgentName().values().toArray(new String[prob.getHighestCostForAgentInBestIterationAgentName().size()]);

            List<String> toPrint = new ArrayList<>();
            for (int i=0; i<average.length; i++)
            {
                toPrint.clear();
                toPrint.add(String.valueOf(i));
                toPrint.add(average[i].toString());
                toPrint.add(total[i].toString());
                toPrint.add(lowestAgentPrice[i].toString());
                toPrint.add(lowestAgentName[i]);
                toPrint.add(highestAgentPrice[i].toString());
                toPrint.add(highestAgentNames[i]);

                CSVUtils.writeLine(writer,toPrint);
            }
        }

        writer.flush();
        writer.close();
    }
}