// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.io;

public class NozzleElement {
    public static final int
        TARGET_ID = 0,
        DIRECTION = 1,
        X = 2,
        Y = 3,
        QUANTITY = 4,
        NUM_ELEMENTS = 5;
    public final int[] elements = new int[NUM_ELEMENTS];

    public NozzleElement(int targetId,int direction,int x,int y,int quantity) {
        elements[TARGET_ID] = targetId;
        elements[DIRECTION] = direction;
        elements[X]         = x;
        elements[Y]         = y;
        elements[QUANTITY]  = quantity;
    }
}
