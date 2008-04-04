package org.basex.api.xqj;

import org.basex.api.xqj.javax.XQException;
import org.basex.api.xqj.javax.XQItemType;
import org.basex.api.xqj.javax.XQStaticContext;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class BXQStaticContext extends DefaultHandler implements XQStaticContext  {

  public void declareNamespace(String arg0, String arg1) throws XQException {
    // TODO Auto-generated method stub
  }

  public String getBaseURI() {
    // TODO Auto-generated method stub
    return null;
  }

  public int getBindingMode() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int getBoundarySpacePolicy() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int getConstructionMode() {
    // TODO Auto-generated method stub
    return 0;
  }

  public XQItemType getContextItemStaticType() {
    // TODO Auto-generated method stub
    return null;
  }

  public int getCopyNamespacesModeInherit() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int getCopyNamespacesModePreserve() {
    // TODO Auto-generated method stub
    return 0;
  }

  public String getDefaultCollation() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getDefaultElementTypeNamespace() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getDefaultFunctionNamespace() {
    // TODO Auto-generated method stub
    return null;
  }

  public int getDefaultOrderForEmptySequences() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int getHoldability() {
    // TODO Auto-generated method stub
    return 0;
  }

  public String[] getNamespacePrefixes() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getNamespaceURI(String arg0) throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public int getOrderingMode() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int getQueryLanguageTypeAndVersion() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int getQueryTimeout() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int getScrollability() {
    // TODO Auto-generated method stub
    return 0;
  }

  public void setBaseURI(String arg0) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void setBindingMode(int arg0) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void setBoundarySpacePolicy(int arg0) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void setConstructionMode(int arg0) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void setContextItemStaticType(XQItemType arg0) {
    // TODO Auto-generated method stub
    
  }

  public void setCopyNamespacesModeInherit(int arg0) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void setCopyNamespacesModePreserve(int arg0) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void setDefaultCollation(String arg0) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void setDefaultElementTypeNamespace(String arg0) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void setDefaultFunctionNamespace(String arg0) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void setDefaultOrderForEmptySequences(int arg0) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void setHoldability(int arg0) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void setOrderingMode(int arg0) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void setQueryLanguageTypeAndVersion(int arg0) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void setQueryTimeout(int arg0) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void setScrollability(int arg0) throws XQException {
    // TODO Auto-generated method stub
    
  }
  
  @Override
  public void startElement(final String uri, final String ln, final String qn,
      final Attributes at) {
    System.out.print("<" + qn);
    for(int a = 0; a < at.getLength(); a++) {
      System.out.print(" " + at.getQName(a) + "=\"" + at.getValue(a) + "\"");
    }
    System.out.print(">");
  }

  @Override
  public void endElement(final String uri, final String ln, final String qn) {
    System.out.print("</" + qn + ">");
  }

  @Override
  public void characters(final char[] ch, final int s, final int l) {
    System.out.print(new String(ch, s, l));
  }
}
