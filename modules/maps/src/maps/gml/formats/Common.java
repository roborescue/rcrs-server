package maps.gml.formats;

import org.dom4j.DocumentHelper;
import org.dom4j.Namespace;
import org.dom4j.QName;

/**
   A bunch of common GML format namespaces and qnames.
*/
public final class Common {
    // CHECKSTYLE:OFF:JavadocVariable
    public static final String GML_NAMESPACE_URI = "http://www.opengis.net/gml";
    public static final String GML_3_2_NAMESPACE_URI = "http://www.opengis.net/gml/3.2";
    public static final String XLINK_NAMESPACE_URI = "http://www.w3.org/1999/xlink";

    public static final Namespace GML_NAMESPACE = DocumentHelper.createNamespace("gml", GML_NAMESPACE_URI);
    public static final Namespace GML_3_2_NAMESPACE = DocumentHelper.createNamespace("gml32", GML_3_2_NAMESPACE_URI);
    public static final Namespace XLINK_NAMESPACE = DocumentHelper.createNamespace("xlink", XLINK_NAMESPACE_URI);

    public static final QName GML_ID_QNAME = DocumentHelper.createQName("id", GML_NAMESPACE);
    public static final QName GML_NODE_QNAME = DocumentHelper.createQName("Node", GML_NAMESPACE);
    public static final QName GML_EDGE_QNAME = DocumentHelper.createQName("Edge", GML_NAMESPACE);
    public static final QName GML_FACE_QNAME = DocumentHelper.createQName("Face", GML_NAMESPACE);
    public static final QName GML_POINT_PROPERTY_QNAME = DocumentHelper.createQName("pointProperty", GML_NAMESPACE);
    public static final QName GML_POINT_QNAME = DocumentHelper.createQName("Point", GML_NAMESPACE);
    public static final QName GML_COORDINATES_QNAME = DocumentHelper.createQName("coordinates", GML_NAMESPACE);
    public static final QName GML_ORIENTATION_QNAME = DocumentHelper.createQName("orientation");
    public static final QName GML_DIRECTED_NODE_QNAME = DocumentHelper.createQName("directedNode", GML_NAMESPACE);
    public static final QName GML_DIRECTED_EDGE_QNAME = DocumentHelper.createQName("directedEdge", GML_NAMESPACE);
    public static final QName GML_DIRECTED_FACE_QNAME = DocumentHelper.createQName("directedFace", GML_NAMESPACE);
    public static final QName GML_CENTRE_LINE_OF_QNAME = DocumentHelper.createQName("centerLineOf", GML_NAMESPACE);
    public static final QName GML_LINE_STRING_QNAME = DocumentHelper.createQName("LineString", GML_NAMESPACE);
    public static final QName GML_POLYGON_QNAME = DocumentHelper.createQName("polygon", GML_NAMESPACE);
    public static final QName GML_LINEAR_RING_QNAME = DocumentHelper.createQName("LinearRing", GML_NAMESPACE);

    public static final QName XLINK_HREF_QNAME = DocumentHelper.createQName("href", XLINK_NAMESPACE);
    // CHECKSTYLE:ON:JavadocVariable

    private Common() {
    }
}
