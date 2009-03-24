// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent.object;

import java.lang.reflect.Method;
import java.util.*;
import yab.agent.Condition;

public abstract class Property {
    public abstract Object eval(Object obj);

    private static final String PACKAGE ="yab.agent.object";
    public static Property get(String className, String methodName) {
        try {
            Class cls = Class.forName(PACKAGE + "." + className);
            final Method method = cls.getMethod(methodName, new Class[0]);
            return new Property() {
                    public Object eval(Object obj) {
                        try { return method.invoke(obj, new Object[0]); }
                        catch (Exception e) { throw new Error(e); } }};
        } catch (Exception e) { throw new Error(e); }
    }

    public Property of(final Property who) {
        final Property whose = this;
        return new Property() {
                public Object eval(Object obj) {
                    return whose.eval(who.eval(obj)); }};
    }

    public Condition eq(final Object value) {
        final Property lhs = this;
        return new Condition() {
                public boolean eval(Object obj) {
                    return lhs.eval(obj) == value; }};
    }
    public Condition equal(final Object value) {
        final Property lhs = this;
        return new Condition() {
                public boolean eval(Object obj) {
                    return lhs.eval(obj).equals(value); }};
    }

    public Condition eq(final int rhs) {
        final Property lhs = this;
        return new Condition() {
                public boolean eval(Object obj) {
                    return ((Integer) lhs.eval(obj)).intValue() == rhs; }};
    }
    public Condition lt(final int rhs) {
        final Property lhs = this;
        return new Condition() {
                public boolean eval(Object obj) {
                    return ((Integer) lhs.eval(obj)).intValue() < rhs; }};
    }
    public Condition gt(final int rhs) {
        final Property lhs = this;
        return new Condition() {
                public boolean eval(Object obj) {
                    return ((Integer) lhs.eval(obj)).intValue() > rhs; }};
    }
    public Condition lte(final int rhs) {
        final Property lhs = this;
        return new Condition() {
                public boolean eval(Object obj) {
                    return ((Integer) lhs.eval(obj)).intValue() <= rhs; }};
    }
    public Condition gte(final int rhs) {
        final Property lhs = this;
        return new Condition() {
                public boolean eval(Object obj) {
                    return ((Integer) lhs.eval(obj)).intValue() >= rhs; }};
    }

    public Condition containedIn(final Collection col) {
        final Property prop = this;
        return new Condition() {
                public boolean eval(Object obj) {
                    return col.contains(prop.eval(obj)); }};
    }

    public Object max(Collection col) {
        if (col.isEmpty()) throw new Error("the col must not be empty.");
        Object result = null;
        int max = Integer.MIN_VALUE;
        for (Iterator it = col.iterator();  it.hasNext();  ) {
            Object obj = it.next();
            int val = ((Integer) eval(obj)).intValue();
            if (val > max) {
                result = obj;
                max = val;
            }
        }
        return result;
    }

    public Object min(Collection col) {
        if (col.isEmpty()) throw new Error("the col must not be empty.");
        Object result = null;
        int min = Integer.MAX_VALUE;
        for (Iterator it = col.iterator();  it.hasNext();  ) {
            Object obj = it.next();
            int val = ((Integer) eval(obj)).intValue();
            if (val < min) {
                result = obj;
                min = val;
            }
        }
        return result;
    }

    public ArrayList collect(Collection col) {
        ArrayList result = new ArrayList(col.size());
        for (Iterator it = col.iterator();  it.hasNext();  )
            result.add(eval(it.next()));
        return result;
    }
}
