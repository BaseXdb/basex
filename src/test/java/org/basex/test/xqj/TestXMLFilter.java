// Copyright (c) 2003, 2006, 2007, 2008 Oracle. All rights reserved.
package org.basex.test.xqj;

import java.io.IOException;
import java.io.StringReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

@SuppressWarnings("all")
public class TestXMLFilter extends XMLFilterImpl {

  InputSource inputSource;

  public TestXMLFilter(final String document) throws SAXException {
    super(XMLReaderFactory.createXMLReader());
    inputSource = new InputSource(new StringReader(document));
  }

  public void parse(final String systemId) throws IOException, SAXException {
    parse(inputSource);
  }
}
