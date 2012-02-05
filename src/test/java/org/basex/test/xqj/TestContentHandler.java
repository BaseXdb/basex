// Copyright (c) 2003, 2006, 2007, 2008 Oracle. All rights reserved.
package org.basex.test.xqj;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

@SuppressWarnings("all")
public class TestContentHandler extends DefaultHandler {

  public StringBuffer buffer = new StringBuffer();

  public void characters(final char[] ch, final int start, final int length) throws SAXException {
    for (int i = 0; i<length; ++i)
      buffer.append(ch[start+i]);
  }

  public void endElement(final String namespaceURI, final String localName, final String qName) throws SAXException {
    buffer.append("</");
    buffer.append(localName);
    buffer.append('>');
  }

  public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes atts) throws SAXException {
    buffer.append('<');
    buffer.append(localName);
    buffer.append('>');
  }
}