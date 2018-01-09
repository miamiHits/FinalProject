package FinalProject.BL.DataCollection;

import java.util.Set;

public class NeighborhoodEpeak {
    private Set<String> Neighborhood;
    private double epeak;

    public NeighborhoodEpeak(Set<String> neighborhood, double epeak) {
        Neighborhood = neighborhood;
        this.epeak = epeak;
    }

    public Set<String> getNeighborhood() {
        return Neighborhood;
    }

    public void setNeighborhood(Set<String> neighborhood) {
        Neighborhood = neighborhood;
    }

    public double getEpeak() {
        return epeak;
    }

    public void setEpeak(double epeak) {
        this.epeak = epeak;
    }
}