package org.basex.api.xqj;

import static org.basex.api.xqj.BXQText.*;
import java.nio.charset.Charset;
import java.util.Set;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQMetaData;

import org.basex.core.Prop;
import org.basex.core.Text;

/**
 * Java XQuery API - Meta Data.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class BXQMetaData extends BXQAbstract implements XQMetaData {
  /**
   * Constructor.
   * @param c close reference
   */
  public BXQMetaData(final BXQAbstract c) {
    super(c);
  }

  @Override
  public int getMaxExpressionLength() throws XQException {
    opened();
    return Integer.MAX_VALUE;
  }

  @Override
  public int getMaxUserNameLength() throws XQException {
    opened();
    return Integer.MAX_VALUE;
  }

  @Override
  public int getProductMajorVersion() throws XQException {
    opened();
    return version(Text.VERSION, true);
  }

  @Override
  public int getProductMinorVersion() throws XQException {
    opened();
    return version(Text.VERSION, false);
  }

  @Override
  public String getProductName() throws XQException {
    opened();
    return Prop.NAME;
  }

  @Override
  public String getProductVersion() throws XQException {
    opened();
    return Text.VERSION;
  }

  @Override
  public Set<String> getSupportedXQueryEncodings() throws XQException {
    opened();
    return Charset.availableCharsets().keySet();
  }

  @Override
  public String getUserName() throws XQException {
    opened();
    return null;
  }

  @Override
  public int getXQJMajorVersion() throws XQException {
    opened();
    return version(VERSION, true);
  }

  @Override
  public int getXQJMinorVersion() throws XQException {
    opened();
    return version(VERSION, false);
  }

  @Override
  public String getXQJVersion() throws XQException {
    opened();
    return VERSION;
  }

  @Override
  public boolean isFullAxisFeatureSupported() throws XQException {
    opened();
    return true;
  }

  @Override
  public boolean isModuleFeatureSupported() throws XQException {
    opened();
    return true;
  }

  @Override
  public boolean isReadOnly() throws XQException {
    opened();
    return false;
  }

  @Override
  public boolean isSchemaImportFeatureSupported() throws XQException {
    opened();
    return false;
  }

  @Override
  public boolean isSchemaValidationFeatureSupported() throws XQException {
    opened();
    return false;
  }

  @Override
  public boolean isSerializationFeatureSupported() throws XQException {
    opened();
    return true;
  }

  @Override
  public boolean isStaticTypingExtensionsSupported() throws XQException {
    opened();
    return false;
  }

  @Override
  public boolean isStaticTypingFeatureSupported() throws XQException {
    opened();
    return false;
  }

  @Override
  public boolean isTransactionSupported() throws XQException {
    opened();
    return false;
  }

  @Override
  public boolean isUserDefinedXMLSchemaTypeSupported() throws XQException {
    opened();
    return false;
  }

  @Override
  public boolean isXQueryEncodingDeclSupported() throws XQException {
    opened();
    return true;
  }

  @Override
  public boolean isXQueryEncodingSupported(final String encoding)
      throws XQException {
    opened();
    return true;
  }

  @Override
  public boolean isXQueryXSupported() throws XQException {
    opened();
    return false;
  }

  @Override
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
  private static int version(final String t, final boolean pre) {
    return Integer.parseInt(t.replaceAll(pre ? ".*\\.| .*" : "\\..*", ""));
  }
}
