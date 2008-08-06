package org.basex.api.xmldb;

import org.basex.BaseX;
import org.basex.core.Context;
import org.basex.data.XMLSerializer;
import org.basex.io.CachedOutput;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xmldb.api.base.Collection;
import org.xmldb.api.modules.XMLResource;
import org.basex.data.Nodes;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import java.io.IOException;
//import java.io.File;
import org.xmldb.api.base.XMLDBException;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
//import org.xml.sax.InputSource;
//import java.io.StringReader;
//import java.io.FileReader;
//import java.io.FileInputStream;

/**
 * Implementation of the XMLResource Interface for the XMLDB:API
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXXMLResource implements XMLResource {
  
  /** Context ctx */
  Context ctx;
  
  /**
   * Standard constructor.
   * @param ctx for Context
   */
  public BXXMLResource(Context ctx) {
    this.ctx = ctx;
  }

  /**
   * @see org.xmldb.api.base.Resource#getContent()
   */
  public Object getContent() {
    Nodes nodes = ctx.current();
    try {
      final CachedOutput out = new CachedOutput();
      final boolean chop = ctx.data().meta.chop;
      nodes.serialize(new XMLSerializer(out, false, chop));
      return out.toString();
    } catch(final Exception ex) {
      BaseX.debug(ex);
    }
    return null;
  }

  /**
   * @see org.xmldb.api.modules.XMLResource#getContentAsDOM()
   */
  public Node getContentAsDOM() throws XMLDBException {
    try
    {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    //factory.setNamespaceAware(true);
    //factory.setValidating(false);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse("");
    return doc;
    } catch(SAXException saxe)
    {
        throw new XMLDBException(1, saxe.getMessage());
    }
    catch(ParserConfigurationException pce)
    {
        throw new XMLDBException(1, pce.getMessage());
    }
    catch(IOException ioe)
    {
        throw new XMLDBException(1, ioe.getMessage());
    }
  }

  /**
   * @see org.xmldb.api.modules.XMLResource#getContentAsSAX(org.xml.sax.ContentHandler)
   */
  public void getContentAsSAX(ContentHandler handler) {
    // TODO Auto-generated method stub
    
  }

  /**
   * @see org.xmldb.api.modules.XMLResource#getDocumentId()
   */
  public String getDocumentId() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.base.Resource#getId()
   */
  public String getId() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.base.Resource#getParentCollection()
   */
  public Collection getParentCollection() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.base.Resource#getResourceType()
   */
  public String getResourceType() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.base.Resource#setContent(java.lang.Object)
   */
  public void setContent(Object value) {
    // TODO Auto-generated method stub
    
  }

  /**
   * @see org.xmldb.api.modules.XMLResource#setContentAsDOM(org.w3c.dom.Node)
   */
  public void setContentAsDOM(Node content) {
    // TODO Auto-generated method stub
    
  }

  /**
   * @see org.xmldb.api.modules.XMLResource#setContentAsSAX()
   */
  public ContentHandler setContentAsSAX() {
    // TODO Auto-generated method stub
    return null;
  }
}
