package FinalProject.BL.DataCollection;

import FinalProject.BL.IterationData.IterationCollectedData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataCollector {

    private Map<String, Integer> numOfAgentsInProblems;
    private Map<ProblemAlgorithm, IterationAgentsPrice> probAlgoToItAgentPrice;
    private Map<ProblemAlgorithm, AlgorithmProblemResult> probAlgoToResult;
    private Map<String, double[]> probToPriceScheme;

    public DataCollector(Map<String, Integer> numOfAgentsInProblems, Map<String, double[]> prices) {
        this.numOfAgentsInProblems = numOfAgentsInProblems;
        this.probToPriceScheme = prices;
        this.probAlgoToItAgentPrice = new HashMap<ProblemAlgorithm, IterationAgentsPrice>();
        this.probAlgoToResult = new HashMap<ProblemAlgorithm, AlgorithmProblemResult>();
}

    public double addData (IterationCollectedData data){
        ProblemAlgorithm tempPA = new ProblemAlgorithm(data.getProblemId(), data.getAlgorithm());
        IterationAgentsPrice tempIAP;
        if (probAlgoToItAgentPrice.containsKey(tempPA)){
            tempIAP = probAlgoToItAgentPrice.get(tempPA);
            tempIAP.addAgentPrice(data.getIterNum(),
                    new AgentPrice(data.getAgentName(), data.getPrice(),
                            data.getPowerConsumptionPerTick()));
        }else{
            tempIAP = new IterationAgentsPrice();
            tempIAP.addAgentPrice(data.getIterNum(),
                    new AgentPrice(data.getAgentName(), data.getPrice(),
                            data.getPowerConsumptionPerTick()));
            probAlgoToItAgentPrice.put(tempPA, tempIAP);
        }
        if (isIterationFinished(tempPA, tempIAP, data)){
            double newPrice = calculateTotalPrice(tempPA, tempIAP, data.getIterNum());
            AlgorithmProblemResult result = probAlgoToResult.get(tempPA);
            if (newPrice < result.getLowestCostInBestIteration()){
                result.setLowestCostInBestIteration(newPrice);
                result.setIterationsTillBestPrice(data.getIterNum());
                setLowestHighestInBestIter(tempPA, result);
                setAvgPriceInIter(tempPA, result, data.getIterNum());
            }else{ //not the best iter
                setAvgPriceInIter(tempPA, result, data.getIterNum());
            }
            return newPrice;
        }
        return 0;
    }

    private void setAvgPriceInIter(ProblemAlgorithm PA, AlgorithmProblemResult result, int iterNum) {
        List<AgentPrice> prices;
        IterationAgentsPrice iter = probAlgoToItAgentPrice.get(PA);
        double avg = 0;
        double price = 0;

        if (iter != null) {
            prices = iter.getAgentsPrices(iterNum);
            for (AgentPrice ag : prices) {
                price = ag.getPrice();
                avg += price;
            }
            result.getAvgPricePerIteration().put(
                    iterNum, avg/prices.size());
        }
    }

    private void setLowestHighestInBestIter(ProblemAlgorithm tempPA, AlgorithmProblemResult result) {
        double avg = 0;
        double min = Double.MAX_VALUE;
        double max = 0;
        double price = 0;
        List<AgentPrice> prices;
        IterationAgentsPrice iter = probAlgoToItAgentPrice.get(tempPA);

        if (iter != null){
            prices = iter.getAgentsPrices(result.getIterationsTillBestPrice());
            for (AgentPrice ag: prices) {
                price = ag.getPrice();
                avg += price;
                min = Double.min(min, price);
                max = Double.max(max, price);
                if (price == min){ //changed min
                    result.setLowestCostForAgentInBestIteration(price);
                    result.setLowestCostForAgentInBestIterationAgentName(ag.getAgentName());
                }
                if (price == max){ //changed max
                    result.setHighestCostForAgentInBestIteration(price);
                    result.setHighestCostForAgentInBestIterationAgentName(ag.getAgentName());
                }
            }
            result.getAvgPricePerIteration().put(
                    result.getIterationsTillBestPrice(), avg/prices.size());
        }
    }

    private double calculateTotalPrice(ProblemAlgorithm tempPA, IterationAgentsPrice IAP, int iterNum) {
        List<AgentPrice> prices = IAP.getAgentsPrices(iterNum);
        List<double[]> agentPrices = prices.stream().map(a -> a.getSchedule()).collect(Collectors.toList());
        double[] priceScheme = probToPriceScheme.get(tempPA.getProblemId());
        return PowerConsumptionUtils.calculateCSum(agentPrices, priceScheme);
    }

    //if first then create new probResult
    private boolean isIterationFinished(ProblemAlgorithm PA, IterationAgentsPrice IAP,
                                     IterationCollectedData data) {
        List<AgentPrice> prices = IAP.getAgentsPrices(data.getIterNum());
        Integer numOfAgents = numOfAgentsInProblems.get(PA.getProblemId());
        if (prices != null && numOfAgents != null &&
                prices.size() == numOfAgents){ //iteration is over
            if (!probAlgoToResult.containsKey(PA)){ //no prob result yet
                AlgorithmProblemResult result = new AlgorithmProblemResult(PA);
                result.setIterationsTillBestPrice(data.getIterNum());
                probAlgoToResult.put(PA, result);
            }
            return true;
        }
        return false;
    }

    public AlgorithmProblemResult getAlgoProblemResult(String problemID, String algoName){
        ProblemAlgorithm PA = new ProblemAlgorithm(problemID, algoName);
        return probAlgoToResult.get(PA);
    }

    public int getNumOfAgentsInProblem(String problemName){
        if (numOfAgentsInProblems.containsKey(problemName)){
            return numOfAgentsInProblems.get(problemName);
        }
        return 0;
    }

    public Map<String, Integer> getNumOfAgentsInProblems() {
        return numOfAgentsInProblems;
    }

    public void setNumOfAgentsInProblems(Map<String, Integer> numOfAgentsInProblems) {
        this.numOfAgentsInProblems = numOfAgentsInProblems;
    }

    public Map<ProblemAlgorithm, IterationAgentsPrice> getProbAlgoToItAgentPrice() {
        return probAlgoToItAgentPrice;
    }

    public void setProbAlgoToItAgentPrice(Map<ProblemAlgorithm, IterationAgentsPrice> probAlgoToItAgentPrice) {
        this.probAlgoToItAgentPrice = probAlgoToItAgentPrice;
    }

}
