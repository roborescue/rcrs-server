package maps;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jscience.geography.coordinates.UTM;
import org.jscience.geography.coordinates.LatLong;
import org.jscience.geography.coordinates.crs.ReferenceEllipsoid;

/**
   Utility class for dealing with maps.
*/
public final class MapTools {
    private MapTools() {
    }

    /**
       Compute the size of one metre in latitude/longitude relative to a reference point.
       @param lat The latitude of the reference point.
       @param lon The longitude of the reference point.
       @return The size of one metre at the reference point.
    */
    public static double sizeOf1Metre(double lat, double lon) {
        UTM centre = UTM.latLongToUtm(LatLong.valueOf(lat, lon, NonSI.DEGREE_ANGLE), ReferenceEllipsoid.WGS84);
        UTM offset = UTM.valueOf(centre.longitudeZone(), centre.latitudeZone(), centre.eastingValue(SI.METRE), centre.northingValue(SI.METRE) + 1, SI.METRE);
        LatLong result = UTM.utmToLatLong(offset, ReferenceEllipsoid.WGS84);
        return Math.abs(result.latitudeValue(NonSI.DEGREE_ANGLE) - lat);
    }
}
