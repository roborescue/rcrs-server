/*
 * Last change: $Date: 2004/05/04 03:09:39 $
 * $Revision: 1.2 $
 * Copyright (c) 2004, The Black Sheep, Department of Computer Science, The
 * University of Auckland
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of The Black Sheep, The Department of Computer Science or
 * The University of Auckland nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior
 * written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package rescuecore.tools.simulationrunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Stack;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLDocumentParser {

  public static XMLTag parse(InputStream in)
      throws XMLDecodingException, IOException {
    try {
      SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
      Handler handler = new Handler();
      parser.parse(new InputSource(in), handler);
      return handler.root;
    } catch (ParserConfigurationException e) {
      throw new XMLDecodingException(e);
    } catch (SAXException e) {
      throw new XMLDecodingException(e);
    }
  }


  public static XMLTag parse(Reader in)
      throws XMLDecodingException, IOException {
    try {
      SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
      Handler handler = new Handler();
      parser.parse(new InputSource(in), handler);
      return handler.root;
    } catch (ParserConfigurationException e) {
      throw new XMLDecodingException(e);
    } catch (SAXException e) {
      throw new XMLDecodingException(e);
    }
  }

  private static class Handler extends DefaultHandler {

    XMLTag root;
    XMLTag current;
    Stack stack;

    public void startDocument() {
      root = null;
      current = null;
      stack = new Stack();
    }


    public void startElement(String uri, String localname, String qName,
        Attributes attributes) {
      if (current != null) {
        stack.push(current);
        XMLTag next = new XMLTag(qName);
        current.addChild(next);
        current = next;
      } else {
        current = new XMLTag(qName);
        root = current;
      }
      for (int i = 0; i < attributes.getLength(); ++i)
        current.setAttribute(attributes.getQName(i), attributes.getValue(i));
    }


    public void endElement(String uri, String localname, String qName) {
      if (stack.size() == 0)
        current = null;
      else
        current = (XMLTag) stack.pop();
    }


    public void characters(char[] data, int start, int length) {
      String s = new String(data, start, length);
      if (s.trim().equals(""))
        return;
      if (current != null)
        current.appendText(s);
    }
  }
}