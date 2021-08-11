package gis2.scenario;

/**
   Interface for a scenario editing function.
*/
public interface Function {
    /**
       Get the name of this function.
       @return The name of the function.
    */
    String getName();

    /**
       Execute this function.
    */
    void execute();
}