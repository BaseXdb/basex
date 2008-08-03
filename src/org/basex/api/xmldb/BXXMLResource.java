package org.basex.api.xmldb;

import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xmldb.api.base.Collection;
import org.xmldb.api.modules.XMLResource;

/**
 * Implementation of the XMLResource Interface for the XMLDB:API
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXXMLResource implements XMLResource {

  /**
   * @see org.xmldb.api.base.Resource#getContent()
   */
  public Object getContent() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.modules.XMLResource#getContentAsDOM()
   */
  public Node getContentAsDOM() {
    // TODO Auto-generated method stub
    return null;
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
