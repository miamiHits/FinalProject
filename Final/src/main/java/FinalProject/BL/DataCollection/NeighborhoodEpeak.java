package FinalProject.BL.DataCollection;

import java.util.Set;

public class NeighborhoodEpeak {
    private Set<String> neighborhood;
    private double epeak;
    private int countEpeaks;

    public NeighborhoodEpeak(Set<String> neighborhood, double epeak) {
        this.neighborhood = neighborhood;
        this.epeak = epeak;
        countEpeaks = 1;
    }

    public boolean gotAllEpeaks(){
        return countEpeaks == neighborhood.size();
    }

    public void addEpeak(){
        countEpeaks ++;
    }

    public Set<String> getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(Set<String> neighborhood) {
        this.neighborhood = neighborhood;
    }

    public double getEpeak() {
        return epeak;
    }

    public void setEpeak(double epeak) {
        this.epeak = epeak;
    }

    public int getCountEpeaks() {
        return countEpeaks;
    }

    public void setCountEpeaks(int countEpeaks) {
        this.countEpeaks = countEpeaks;
    }
}
