package FinalProject.BL;

import FinalProject.BL.DataObjects.Problem;

import java.util.ArrayList;
import java.util.List;

public class ProblemLoadResult {

    private final List<Problem> problems = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<Problem> getProblems() {
        return problems;
    }

    public void addProblem(Problem problem) {
        synchronized (problems) {
            this.problems.add(problem);
        }
    }

    public List<String> getErrors() {
        return errors;
    }

    public synchronized void addError(String error) {
        synchronized (errors) {
            errors.add(error);
        }
    }
}
