package org.basex.api.jaxp;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import org.basex.util.Util;
import org.xml.sax.SAXException;

/**
 * This class provides an entry to the projects' JAXP implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class BXDocBuilderFactory extends DocumentBuilderFactory {
  @Override
  public DocumentBuilder newDocumentBuilder()
      throws ParserConfigurationException {
    try {
      return new BXDocBuilder();
    } catch(final SAXException se) {
      throw new ParserConfigurationException(se.getMessage());
    }
  }

  @Override
  public void setAttribute(final String name, final Object value) {
    throw Util.notimplemented();
  }

  @Override
  public Object getAttribute(final String name) {
    throw Util.notimplemented();
  }

  @Override
  public Schema getSchema() {
    throw Util.notimplemented();
  }

  @Override
  public void setSchema(final Schema grammar) {
    throw Util.notimplemented();
  }

  @Override
  public boolean isXIncludeAware() {
    throw Util.notimplemented();
  }

  @Override
  public void setXIncludeAware(final boolean state) {
    throw Util.notimplemented();
  }

  @Override
  public boolean getFeature(final String name) {
    throw Util.notimplemented();
  }

  @Override
  public void setFeature(final String name, final boolean value) {
    throw Util.notimplemented();
  }

  // doesn't overwrite its ancestor method..
  public static DocumentBuilderFactory newInstance() {
    return new BXDocBuilderFactory();
  }
}
