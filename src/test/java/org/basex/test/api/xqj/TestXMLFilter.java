// Copyright (c) 2003, 2006, 2007, 2008 Oracle. All rights reserved.
package org.basex.test.api.xqj;

import java.io.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

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
