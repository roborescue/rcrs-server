/*
 * Last change: $Date: 2004/05/04 03:09:39 $
 * $Revision: 1.2 $
 *
 * Copyright (c) 2004, The Black Sheep, Department of Computer Science, The University of Auckland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of The Black Sheep, The Department of Computer Science or The University of Auckland nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package rescuecore.tools.simulationrunner;

import java.util.*;
import java.io.*;

public class XMLTag {
    private XMLTag parent;
    private List children;
    private String name;
    private Map attributes;
    private StringBuffer textBuffer;
    private String text;

    public XMLTag(String name) {
		this.name = name;
		attributes = new HashMap();
		textBuffer = new StringBuffer();
		children = new ArrayList();
    }

    public XMLTag(String name, String text) {
		this.name = name;
		attributes = new HashMap();
		textBuffer = new StringBuffer();
		children = new ArrayList();
		appendText(text);
    }

    public String getName() {
		return name;
    }

    public void setAttribute(String name, String value) {
		attributes.put(name,value);
    }

    public String getAttribute(String name) {
		return (String)attributes.get(name);
    }

    public void appendText(String s) {
		textBuffer.append(s);
		text = null;
    }

    public String getText() {
		if (text==null) text = textBuffer.toString();
		return text;
    }

    public void setParent(XMLTag parent) {
		this.parent = parent;
    }

    public XMLTag getParent() {
		return parent;
    }

    public void addChild(XMLTag child) {
		children.add(child);
		child.parent = this;
    }

    public List getChildren() {
		return children;
    }

    public List getChildren(String name) {
		List result = new ArrayList();
		for (Iterator it = children.iterator();it.hasNext();) {
			XMLTag next = (XMLTag)it.next();
			if (next.name.equals(name)) result.add(next);
		}
		return result;
    }

    public XMLTag getChild(String name) throws XMLDecodingException {
		return getChild(name,true);
    }

    public XMLTag getChild(String name, boolean canBeNull) throws XMLDecodingException {
		List allChildren = getChildren(name);
		if (allChildren.size()>1) throw new XMLDecodingException(this+" should have no more than one child called "+name);
		if (allChildren.size()==0) {
			if (canBeNull) return null;
			throw new XMLDecodingException(this+" should have exactly one child called "+name);
		}
		return (XMLTag)allChildren.get(0);
    }

    public void prettyPrint(PrintWriter out, String prefix) {
		prettyPrint(this,out,prefix);
    }

    public void print(PrintWriter out) {
		print(this,out);
    }

    public String toString() {
		StringWriter string = new StringWriter();
		PrintWriter out = new PrintWriter(string);
		print(out);
		return string.toString();
    }

    public static void print(XMLTag tag, PrintWriter out) {
		// If this is an empty tag then use the <tagname/> form
		if (tag.children.size()==0 && tag.attributes.size()==0 && tag.getText().equals("")) {
			out.print("<");
			out.print(tag.name);
			out.print("/>");
			return;
		}
		// Start tag
		out.print("<");
		out.print(tag.name);
		// Attributes
		for (Iterator it = tag.attributes.keySet().iterator();it.hasNext();) {
			out.print(" ");
			String name = (String)it.next();
			String value = (String)tag.attributes.get(name);
			char quoteChar = '"';
			if (value.indexOf("\"")!=-1) quoteChar='\'';
			out.print(name);
			out.print("=");
			out.print(quoteChar);
			out.print(value);
			out.print(quoteChar);
		}
		out.print(">");
		out.print(tag.getText());
		// Children
		for (Iterator it = tag.children.iterator();it.hasNext();) {
			print((XMLTag)it.next(),out);
		}
		// End tag
		out.print("</");
		out.print(tag.name);
		out.print(">");
    }

    public static void prettyPrint(XMLTag tag, PrintWriter out) {
		prettyPrint(tag,out,"");
    }

    public static void prettyPrint(XMLTag tag, PrintWriter out, String prefix) {
		// If this is an empty tag then use the <tagname/> form
		if (tag.children.size()==0 && tag.attributes.size()==0 && tag.getText().equals("")) {
			out.print(prefix);
			out.print("<");
			out.print(tag.name);
			out.println("/>");
			return;
		}
		// Start tag
		out.print(prefix);
		out.print("<");
		out.print(tag.name);
		// Attributes
		for (Iterator it = tag.attributes.keySet().iterator();it.hasNext();) {
			out.print(" ");
			String name = (String)it.next();
			String value = (String)tag.attributes.get(name);
			char quoteChar = '"';
			if (value.indexOf("\"")!=-1) quoteChar='\'';
			out.print(name);
			out.print("=");
			out.print(quoteChar);
			out.print(value);
			out.print(quoteChar);
		}
		out.print(">");
		// Text
		String text = tag.getText();
		if (text==null || text.trim().equals("")) out.println();
		else out.print(text);
		// Children
		if (tag.children.size()>0) {
			String childPrefix = prefix+"  ";
			for (Iterator it = tag.children.iterator();it.hasNext();) {
				prettyPrint((XMLTag)it.next(),out,childPrefix);
			}
			// End tag
			out.print(prefix);
		}
		out.print("</");
		out.print(tag.name);
		out.println(">");
    }
}
