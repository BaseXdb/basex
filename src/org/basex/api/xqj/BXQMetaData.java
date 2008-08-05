package org.basex.api.xqj;

import static org.basex.api.xqj.BXQText.*;
import java.nio.charset.Charset;
import java.util.Set;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQMetaData;
import org.basex.Text;

/**
 * Java XQuery API - Meta Data.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXQMetaData extends BXQClose implements XQMetaData {
  /**
   * Constructor.
   * @param c close reference
   */
  public BXQMetaData(final BXQClose c) {
    super(c);
  }

  public int getMaxExpressionLength() throws XQException {
    check();
    return Integer.MAX_VALUE;
  }

  public int getMaxUserNameLength() throws XQException {
    check();
    return Integer.MAX_VALUE;
  }

  public int getProductMajorVersion() throws XQException {
    check();
    return version(Text.VERSION, true);
  }

  public int getProductMinorVersion() throws XQException {
    check();
    return version(Text.VERSION, false);
  }

  public String getProductName() throws XQException {
    check();
    return Text.NAME;
  }

  public String getProductVersion() throws XQException {
    check();
    return Text.VERSION;
  }

  public Set getSupportedXQueryEncodings() throws XQException {
    check();
    return Charset.availableCharsets().keySet();
  }

  public String getUserName() throws XQException {
    check();
    return null;
  }

  public int getXQJMajorVersion() throws XQException {
    check();
    return version(VERSION, true);
  }

  public int getXQJMinorVersion() throws XQException {
    check();
    return version(VERSION, false);
  }

  public String getXQJVersion() throws XQException {
    check();
    return VERSION;
  }

  public boolean isFullAxisFeatureSupported() throws XQException {
    check();
    return true;
  }

  public boolean isModuleFeatureSupported() throws XQException {
    check();
    return true;
  }

  public boolean isReadOnly() throws XQException {
    check();
    return true;
  }

  public boolean isSchemaImportFeatureSupported() throws XQException {
    check();
    return false;
  }

  public boolean isSchemaValidationFeatureSupported() throws XQException {
    check();
    return false;
  }

  public boolean isSerializationFeatureSupported() throws XQException {
    check();
    return false;
  }

  public boolean isStaticTypingExtensionsSupported() throws XQException {
    check();
    return false;
  }

  public boolean isStaticTypingFeatureSupported() throws XQException {
    check();
    return false;
  }

  public boolean isTransactionSupported() throws XQException {
    check();
    return false;
  }

  public boolean isUserDefinedXMLSchemaTypeSupported() throws XQException {
    check();
    return false;
  }

  public boolean isXQueryEncodingDeclSupported() throws XQException {
    check();
    return true;
  }

  public boolean isXQueryEncodingSupported(String encoding) throws XQException {
    check();
    return true;
  }

  public boolean isXQueryXSupported() throws XQException {
    check();
    return false;
  }

  public boolean wasCreatedFromJDBCConnection() throws XQException {
    check();
    return false;
  }

  /**
   * Returns the pre- or suffix of the specified version.
   * @param t input version
   * @param pre flag
   * @return pre or suffix
   */
  private int version(final String t, final boolean pre) {
    return Integer.parseInt(t.replaceAll(pre ? ".*\\." : "\\..*", ""));
  }
}
