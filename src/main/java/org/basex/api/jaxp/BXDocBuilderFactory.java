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
public final class BXDocBuilderFactory extends DocumentBuilderFactory {
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
    Util.notimplemented();
  }

  @Override
  public Object getAttribute(final String name) {
    Util.notimplemented();
    return null;
  }

  @Override
  public Schema getSchema() {
    Util.notimplemented();
    return null;
  }

  @Override
  public void setSchema(final Schema grammar) {
    Util.notimplemented();
  }

  @Override
  public boolean isXIncludeAware() {
    Util.notimplemented();
    return false;
  }

  @Override
  public void setXIncludeAware(final boolean state) {
    Util.notimplemented();
  }

  @Override
  public boolean getFeature(final String name) {
    Util.notimplemented();
    return false;
  }

  @Override
  public void setFeature(final String name, final boolean value) {
    Util.notimplemented();
  }

  // doesn't overwrite its ancestor method..
  public static DocumentBuilderFactory newInstance() {
    return new BXDocBuilderFactory();
  }
}
