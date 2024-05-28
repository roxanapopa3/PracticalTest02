package ro.pub.cs.systems.eim.practical2test;

public class EarthquakeInformation {

    private String definition;

    public EarthquakeInformation() {
        this.definition = null;

    }

    public EarthquakeInformation(String definition) {
        this.definition = definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getDefinition() {
        return definition;
    }

    @Override
    public String toString() {
        return "Definition" +
                definition;
    }
}
