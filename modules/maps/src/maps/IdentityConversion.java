package maps;

/**
   A no-op coordinate conversion.
*/
public class IdentityConversion implements CoordinateConversion {
    @Override
    public double convertX(double x) {
        return x;
    }

    @Override
    public double convertY(double y) {
        return y;
    }
}
