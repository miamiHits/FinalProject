package FinalProject.PL.UIEntities;

import java.util.Objects;

public class SelectedProblem {

    private String name;
    private int size;

    public SelectedProblem(String name, int size) {
        this.name = name;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SelectedProblem that = (SelectedProblem) o;
        return size == that.size &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, size);
    }
}
