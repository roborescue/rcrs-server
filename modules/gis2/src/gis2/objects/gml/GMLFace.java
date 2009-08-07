package gis2.objects.gml;

public class GMLFace extends GMLObject {

    private GMLDirectedEdge[] dedges;
    private String type;

    public GMLFace(GMLID id, GMLDirectedEdge[] es) {
        super(id);
        if (es == null) {
            throw new NullPointerException();
        }
        setDirectedEdges(es);
    }

    public void setType(String t) {
        type = t;
    }

    public String getType() {
        return type;
    }

    public void setDirectedEdges(GMLDirectedEdge[] es) {
        dedges = es;
    }

    public GMLDirectedEdge[] getDirectedEdges() {
        return dedges;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(dedges[0]);
        for (int i = 1; i < dedges.length; i++) {
            sb.append(",").append(dedges[i]);
        }
        return "GMLFace[" + sb + "]";
    }
}