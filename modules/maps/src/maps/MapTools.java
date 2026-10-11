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

    public static double sizeOf1MetreLatitude(double lat, double lon) {
        UTM centre = UTM.latLongToUtm(LatLong.valueOf(lat, lon, NonSI.DEGREE_ANGLE), ReferenceEllipsoid.WGS84);
        UTM offset = UTM.valueOf(centre.longitudeZone(), centre.latitudeZone(),
                centre.eastingValue(SI.METRE), centre.northingValue(SI.METRE) + 1, SI.METRE);
        LatLong result = UTM.utmToLatLong(offset, ReferenceEllipsoid.WGS84);
        return Math.abs(result.latitudeValue(NonSI.DEGREE_ANGLE) - lat);
    }

    public static double sizeOf1MetreLongitude(double lat, double lon) {
        UTM centre = UTM.latLongToUtm(LatLong.valueOf(lat, lon, NonSI.DEGREE_ANGLE), ReferenceEllipsoid.WGS84);
        UTM offset = UTM.valueOf(centre.longitudeZone(), centre.latitudeZone(),
                centre.eastingValue(SI.METRE) + 1, centre.northingValue(SI.METRE), SI.METRE);
        LatLong result = UTM.utmToLatLong(offset, ReferenceEllipsoid.WGS84);
        return Math.abs(result.longitudeValue(NonSI.DEGREE_ANGLE) - lon);
    }
}
