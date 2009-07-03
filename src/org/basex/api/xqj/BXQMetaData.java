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
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public class BXQMetaData extends BXQAbstract implements XQMetaData {
  /**
   * Constructor.
   * @param c close reference
   */
  public BXQMetaData(final BXQAbstract c) {
    super(c);
  }

  public int getMaxExpressionLength() throws XQException {
    opened();
    return Integer.MAX_VALUE;
  }

  public int getMaxUserNameLength() throws XQException {
    opened();
    return Integer.MAX_VALUE;
  }

  public int getProductMajorVersion() throws XQException {
    opened();
    return version(Text.VERSION, true);
  }

  public int getProductMinorVersion() throws XQException {
    opened();
    return version(Text.VERSION, false);
  }

  public String getProductName() throws XQException {
    opened();
    return Text.NAME;
  }

  public String getProductVersion() throws XQException {
    opened();
    return Text.VERSION;
  }

  public Set<String> getSupportedXQueryEncodings() throws XQException {
    opened();
    return Charset.availableCharsets().keySet();
  }

  public String getUserName() throws XQException {
    opened();
    return null;
  }

  public int getXQJMajorVersion() throws XQException {
    opened();
    return version(VERSION, true);
  }

  public int getXQJMinorVersion() throws XQException {
    opened();
    return version(VERSION, false);
  }

  public String getXQJVersion() throws XQException {
    opened();
    return VERSION;
  }

  public boolean isFullAxisFeatureSupported() throws XQException {
    opened();
    return true;
  }

  public boolean isModuleFeatureSupported() throws XQException {
    opened();
    return true;
  }

  public boolean isReadOnly() throws XQException {
    opened();
    return true;
  }

  public boolean isSchemaImportFeatureSupported() throws XQException {
    opened();
    return false;
  }

  public boolean isSchemaValidationFeatureSupported() throws XQException {
    opened();
    return false;
  }

  public boolean isSerializationFeatureSupported() throws XQException {
    opened();
    return true;
  }

  public boolean isStaticTypingExtensionsSupported() throws XQException {
    opened();
    return false;
  }

  public boolean isStaticTypingFeatureSupported() throws XQException {
    opened();
    return false;
  }

  public boolean isTransactionSupported() throws XQException {
    opened();
    return false;
  }

  public boolean isUserDefinedXMLSchemaTypeSupported() throws XQException {
    opened();
    return false;
  }

  public boolean isXQueryEncodingDeclSupported() throws XQException {
    opened();
    return true;
  }

  public boolean isXQueryEncodingSupported(final String encoding)
      throws XQException {
    opened();
    return true;
  }

  public boolean isXQueryXSupported() throws XQException {
    opened();
    return false;
  }

  public boolean wasCreatedFromJDBCConnection() throws XQException {
    opened();
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
