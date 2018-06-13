package FinalProject.DAL;

import FinalProject.BL.DataCollection.AlgorithmProblemResult;
import FinalProject.Config;
import FinalProject.Service;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;


public class CsvHandler implements FileSaverInterface {
    private final static Logger logger = Logger.getLogger(Service.class);

    private Map<String, List<Double>> totalPowerConsumption;
    private Map<String, List<Double>> totalPowerConsumptionAnyTime;
    private Map<String, List<Double>> highestAgent;
    private Map<String, List<Double>> lowestAgent;
    private Map<String, List<Long>> averageTimePerIter;
    private Map<String, Long> totalMessagesSize;
    private Map<String, Long> totalAverageMessages;

    private int ITER_NUM;
    private static final String RESULTS_PATH = Config.getStringPropery(Config.REPORTS_OUT_DIR).replaceAll("/", Matcher.quoteReplacement(Matcher.quoteReplacement(File.separator)));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd--MM--yyyy_HH-mm");

    public CsvHandler(Map<String, List<Double>> totalPowerConsumption)
    {
        this.totalPowerConsumption = totalPowerConsumption;
    }

    public CsvHandler() {
    }

    public void setTotalPowerConsumption(Map<String, List<Double>> totalPowerConsumption) {
        this.totalPowerConsumption = totalPowerConsumption;
    }

    public void setTotalPowerConsumptionAnyTime(Map<String, List<Double>> totalPowerConsumptionAnyTime) {
        this.totalPowerConsumptionAnyTime = totalPowerConsumptionAnyTime;
    }

    public void setHighestAgent(Map<String, List<Double>> highestAgent) {
        this.highestAgent = highestAgent;
    }

    public void setLowestAgent(Map<String, List<Double>> lowestAgent) {
        this.lowestAgent = lowestAgent;
    }

    public void setAverageTimePerIter(Map<String, List<Long>> averageTimePerIter) {
        this.averageTimePerIter = averageTimePerIter;
    }

    public void setTotalMessagesSize(Map<String, Long> totalMessagesSize) {
        this.totalMessagesSize = totalMessagesSize;
    }

    public void setTotalAverageMessages(Map<String, Long> totalAverageMessages) {
        this.totalAverageMessages = totalAverageMessages;
    }

    @Override
    public void saveExpirmentResult(List<AlgorithmProblemResult> problemResults) throws IOException {
        FileWriter writer = new FileWriter(RESULTS_PATH + dateFormat.format(new Date())+"_results.csv", true);

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
            writer.write("BEST Total Grade,");
            writer.write("Lowest Agent Price,");
            writer.write("Lowest Agent Name,");
            writer.write("Highest Agent,");
            writer.write("Highest Agent Name,");
            writer.write("\n");
            Double[] average = prob.getAvgPricePerIteration().values().toArray(new Double[prob.getAvgPricePerIteration().size()]);
            this.ITER_NUM = average.length;
           // writer.write("DEBUG: AVE " + prob.getAvgPricePerIteration().size() +",");
            Double[] total = prob.getTotalGradePerIteration().values().toArray(new Double[prob.getTotalGradePerIteration().size()]);
            //writer.write("DEBUG: total " + prob.getTotalGradePerIteration().size() +",");
            Double[] bestTotal = prob.getBestTotalGradePerIter().values().toArray(new Double[prob.getTotalGradePerIteration().size()]);

            Double[] lowestAgentPrice = prob.getLowestCostForAgentInBestIteration().values().toArray(new Double[prob.getLowestCostForAgentInBestIteration().size()]);
            //writer.write("DEBUG: lowestAgentPrice " + prob.getLowestCostForAgentInBestIteration().size() +",");
            String[] lowestAgentName = prob.getLowestCostForAgentInBestIterationAgentName().values().toArray(new String[prob.getLowestCostForAgentInBestIterationAgentName().size()]);
            //writer.write("DEBUG: lowestAgentName " + prob.getLowestCostForAgentInBestIterationAgentName().size() +",");
            Double[] highestAgentPrice = prob.getHighestCostForAgentInBestIteration().values().toArray(new Double[prob.getHighestCostForAgentInBestIteration().size()]);
            //writer.write("DEBUG: highestAgentPrice " + prob.getHighestCostForAgentInBestIteration().size() +",");
            String[] highestAgentNames = prob.getHighestCostForAgentInBestIterationAgentName().values().toArray(new String[prob.getHighestCostForAgentInBestIterationAgentName().size()]);

            List<String> toPrint = new ArrayList<>();
            for (int i=0; i<ITER_NUM; i++)
            {
                toPrint.clear();
                toPrint.add(String.valueOf(i));
                toPrint.add(average[i].toString());
                toPrint.add(total[i].toString());
                toPrint.add(bestTotal[i].toString());
                toPrint.add(lowestAgentPrice[i].toString());
                toPrint.add(lowestAgentName[i]);
                toPrint.add(highestAgentPrice[i].toString());
                toPrint.add(highestAgentNames[i]);

                writeLine(writer,toPrint);
            }

            writer.write("\n");
            writer.write("\n");
            writer.write("\n");
        }

        writer.write("Average total power consumption per algorithm per iteration");
        writer.write("\n");
        writer.write("\n");
        writer.write("Iteration number,");
        for(String algoName : this.totalPowerConsumption.keySet())
        {
            writer.write(algoName+",");
        }

        writer.write("\n");
        List<String> toPrint = new ArrayList<>();
        for( int i =0; i<ITER_NUM; i++)
        {
            toPrint.clear();
            toPrint.add(String.valueOf(i));
            for(Map.Entry<String, List<Double>> entry : this.totalPowerConsumption.entrySet())
            {
                toPrint.add(entry.getValue().get(i).toString());
            }

            writeLine(writer,toPrint);

        }

        writer.write("\n");

        writer.write(" Average BEST total power consumption per algorithm per iteration");
        writer.write("\n");
        writer.write("\n");
        writer.write("Iteration number,");
        for(String algoName : this.totalPowerConsumptionAnyTime.keySet())
        {
            writer.write(algoName+",");
        }

        writer.write("\n");
        toPrint = new ArrayList<>();
        for( int i =0; i<ITER_NUM; i++)
        {
            toPrint.clear();
            toPrint.add(String.valueOf(i));
            for(Map.Entry<String, List<Double>> entry : this.totalPowerConsumptionAnyTime.entrySet())
            {
                toPrint.add(entry.getValue().get(i).toString());
            }

            writeLine(writer,toPrint);

        }

        writer.write("\n");

        writer.write("Highest Agent per algorithm per iteration");
        writer.write("\n");
        writer.write("\n");
        writer.write("Iteration number,");
        for(String algoName : this.highestAgent.keySet())
        {
            writer.write(algoName+",");
        }

        writer.write("\n");
        toPrint = new ArrayList<>();
        for( int i =0; i<ITER_NUM; i++)
        {
            toPrint.clear();
            toPrint.add(String.valueOf(i));
            for(Map.Entry<String, List<Double>> entry : this.highestAgent.entrySet())
            {
                toPrint.add(entry.getValue().get(i).toString());
            }

            writeLine(writer,toPrint);

        }

        writer.write("\n");

        writer.write("Lowest Agent per algorithm per iteration");
        writer.write("\n");
        writer.write("\n");
        writer.write("Iteration number,");
        for(String algoName : this.lowestAgent.keySet())
        {
            writer.write(algoName+",");
        }

        writer.write("\n");
        toPrint = new ArrayList<>();
        for( int i =0; i<ITER_NUM; i++)
        {
            toPrint.clear();
            toPrint.add(String.valueOf(i));
            for(Map.Entry<String, List<Double>> entry : this.lowestAgent.entrySet())
            {
                toPrint.add(entry.getValue().get(i).toString());
            }

            writeLine(writer,toPrint);

        }

        writer.write("\n");

        writer.write(" Average time per algorithm per iteration");
        writer.write("\n");
        writer.write("\n");
        writer.write("Iteration number,");
        for(String algoName : this.averageTimePerIter.keySet())
        {
            writer.write(algoName+",");
        }
        writer.write("\n");
        toPrint = new ArrayList<>();
        for( int i =0; i<ITER_NUM; i++)
        {
            toPrint.clear();
            toPrint.add(String.valueOf(i));
            for(Map.Entry<String, List<Long>> entry : this.averageTimePerIter.entrySet())
            {
                toPrint.add(entry.getValue().get(i).toString());
            }

            writeLine(writer,toPrint);

        }

        writer.write("\n");

        writer.write(" Average Messages Number per algorithm over all iterations");
        writer.write("\n");
        writer.write("\n");
        writer.write("Algo Name,");

        for(Map.Entry<String, Long> entry : this.totalAverageMessages.entrySet())
        {
            writer.write(entry.getKey()+",");
        }
        writer.write("\n");
        writer.write("Number of average massages,");

        for(Map.Entry<String, Long> entry : this.totalAverageMessages.entrySet())
        {
            writer.write(entry.getValue().toString()+",");
        }

        writer.write("\n");

        writer.write(" Average Messages size per algorithm over all iterations");
        writer.write("\n");
        writer.write("\n");
        writer.write("Algo Name,");

        for(Map.Entry<String, Long> entry : this.totalMessagesSize.entrySet())
        {
            writer.write(entry.getKey()+",");
        }
        writer.write("\n");
        writer.write("size (bytes) of massages,");

        for(Map.Entry<String, Long> entry : this.totalMessagesSize.entrySet())
        {
            writer.write(entry.getValue().toString()+",");

        }

        writer.flush();
        writer.close();
    }

    private final char DEFAULT_SEPARATOR = ',';

    private void writeLine(Writer w, List<String> values) throws IOException {
        writeLine(w, values, DEFAULT_SEPARATOR, ' ');
    }

    private String followCVSformat(String value) {

        String result = value;
        if (result.contains("\"")) {
            result = result.replace("\"", "\"\"");
        }
        return result;

    }

    private void writeLine(Writer w, List<String> values, char separators, char customQuote) throws IOException {

        boolean first = true;

        //default customQuote is empty

        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (!first) {
                sb.append(separators);
            }
            if (customQuote == ' ') {
                sb.append(followCVSformat(value));
            } else {
                sb.append(customQuote).append(followCVSformat(value)).append(customQuote);
            }

            first = false;
        }
        sb.append("\n");
        w.append(sb.toString());


    }

}