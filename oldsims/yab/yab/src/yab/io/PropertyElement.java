// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.io;

public class PropertyElement {
    public final int type;
	public final int length;
    public final int[] value;

    public PropertyElement(int type, int length, int[] value) {
        this.type = type;
	this.length = length;
        this.value = value;
    }
}
