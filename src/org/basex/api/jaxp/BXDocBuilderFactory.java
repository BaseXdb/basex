package org.basex.api.jaxp;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import org.basex.BaseX;
import org.xml.sax.SAXException;

/**
 * This class provides an entry to the projects' JAXP implementation.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class BXDocBuilderFactory extends DocumentBuilderFactory {
  @Override
  public DocumentBuilder newDocumentBuilder()
      throws ParserConfigurationException {
    try {
      return new BXDocBuilder();
    } catch (SAXException se) {
      throw new ParserConfigurationException(se.getMessage());
    }
  }

  @Override
  public void setAttribute(String name, Object value) {
    BaseX.notimplemented();
  }

  @Override
  public Object getAttribute(String name) {
    BaseX.notimplemented();
    return null;
  }

  @Override
  public Schema getSchema() {
    BaseX.notimplemented();
    return null;
  }

  @Override
  public void setSchema(Schema grammar) {
    BaseX.notimplemented();
  }

  @Override
  public boolean isXIncludeAware() {
    BaseX.notimplemented();
    return false;
  }

  @Override
  public void setXIncludeAware(boolean state) {
    BaseX.notimplemented();
  }

  @Override
  public boolean getFeature(String name) {
    BaseX.notimplemented();
    return false;
  }

  @Override
  public void setFeature(String name, boolean value) {
    BaseX.notimplemented();
  }
  
  // doesn't overwrite its ancestor method..
  public static DocumentBuilderFactory newInstance() {
    return new BXDocBuilderFactory();
  }
}
