package FinalProject.DAL;

import jade.core.behaviours.Behaviour;

import java.util.List;

public interface AlgoLoaderInterface {

    List<Behaviour> loadAlgorithms(List<String> algoNames); //TODO change to correct type
    List<String> getAllAlgoNames();
    void addAlgoToSystem(String path, String fileName);

}
