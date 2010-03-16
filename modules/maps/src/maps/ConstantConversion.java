package maps;

/**
   A coordinate conversion that multiplies by a number.
*/
public class ConstantConversion implements CoordinateConversion {
    private double constant;

    /**
       Construct a ConstantConversion.
       @param c The constant.
    */
    public ConstantConversion(double c) {
        constant = c;
    }

    @Override
    public double convertX(double x) {
        return x * constant;
    }

    @Override
    public double convertY(double y) {
        return y * constant;
    }
}
