// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent;

import java.util.*;

public abstract class Condition {
    public abstract boolean eval(Object obj);

    public ArrayList extract(Collection col) {
        ArrayList result = new ArrayList();
        for (Iterator it = col.iterator();  it.hasNext();  ) {
            Object obj = it.next();
            if (eval(obj))
                result.add(obj);
        }
        return result;
    }

    public Condition and(final Condition rhs) {
        final Condition lhs = this;
        return new Condition() {
                public boolean eval(Object obj) {
                    return lhs.eval(obj) && rhs.eval(obj); }};
    }
    public Condition or(final Condition rhs) {
        final Condition lhs = this;
        return new Condition() {
                public boolean eval(Object obj) {
                    return lhs.eval(obj) || rhs.eval(obj); }};
    }
    public Condition not() {
        final Condition cond = this;
        return new Condition() {
                public boolean eval(Object obj) { return !cond.eval(obj); }};
    }
}
