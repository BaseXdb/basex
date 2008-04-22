package org.basex.api.xqj;

import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQStaticContext;
import org.basex.core.Context;

/**
 * BaseX XQuery static context.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXQStaticContext implements XQStaticContext {
  /** BaseX data reference. */
  Context ctx;

  /**
   * Constructor, specifying a context instance.
   * @param c context instance
   */
  public BXQStaticContext(final Context c) {
    ctx = c;
  }

  public void declareNamespace(String arg0, String arg1) {
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

  public String getNamespaceURI(String arg0) {
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

  public void setBaseURI(String arg0) {
    // TODO Auto-generated method stub
    
  }

  public void setBindingMode(int arg0) {
    // TODO Auto-generated method stub
    
  }

  public void setBoundarySpacePolicy(int arg0) {
    // TODO Auto-generated method stub
    
  }

  public void setConstructionMode(int arg0) {
    // TODO Auto-generated method stub
    
  }

  public void setContextItemStaticType(XQItemType arg0) {
    // TODO Auto-generated method stub
    
  }

  public void setCopyNamespacesModeInherit(int arg0) {
    // TODO Auto-generated method stub
    
  }

  public void setCopyNamespacesModePreserve(int arg0) {
    // TODO Auto-generated method stub
    
  }

  public void setDefaultCollation(String arg0) {
    // TODO Auto-generated method stub
    
  }

  public void setDefaultElementTypeNamespace(String arg0) {
    // TODO Auto-generated method stub
    
  }

  public void setDefaultFunctionNamespace(String arg0) {
    // TODO Auto-generated method stub
    
  }

  public void setDefaultOrderForEmptySequences(int arg0) {
    // TODO Auto-generated method stub
    
  }

  public void setHoldability(int arg0) {
    // TODO Auto-generated method stub
    
  }

  public void setOrderingMode(int arg0) {
    // TODO Auto-generated method stub
    
  }

  public void setQueryLanguageTypeAndVersion(int arg0) {
    // TODO Auto-generated method stub
    
  }

  public void setQueryTimeout(int arg0) {
    // TODO Auto-generated method stub
    
  }

  public void setScrollability(int arg0) {
    // TODO Auto-generated method stub
    
  }
}
