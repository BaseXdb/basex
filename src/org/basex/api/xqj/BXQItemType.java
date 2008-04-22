package org.basex.api.xqj;

import java.net.URI;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQItemType;
import org.basex.query.xquery.item.Type;

/**
 * BaseX  XQuery item type.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXQItemType implements XQItemType {
  
  /** Type itemType. */
  Type itemType;

  public int getBaseType() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int getItemKind() {
    return itemType.ordinal();
  }

  public int getItemOccurrence() {
    // TODO Auto-generated method stub
    return 0;
  }

  public QName getNodeName() {
    if(itemType.node()) {
      return new QName(itemType.name());
    }
    return null;
  }

  public String getPIName() {
    // TODO Auto-generated method stub
    return null;
  }

  public URI getSchemaURI() {
    // TODO Auto-generated method stub
    return null;
  }

  public QName getTypeName() {
    return new QName(new String(itemType.name));
  }

  public boolean isAnonymousType() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isElementNillable() {
    // TODO Auto-generated method stub
    return false;
  }

  public XQItemType getItemType() {
    return this;
  }
}
