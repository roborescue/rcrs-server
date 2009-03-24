// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.io;

public class ObjectElement {
    public final int type;
	public final int length;
    public final int id;
    public final PropertyElement[] properties;

    public ObjectElement(int type, int length, int id, PropertyElement[] properties) {
        this.type = type;
	this.length = length;
        this.id = id;
        this.properties = properties;
    }
}
