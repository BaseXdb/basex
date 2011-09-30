package org.basex.api.dom;

import org.basex.util.Token;
import org.basex.util.Util;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DocumentType;

/**
 * DOM - implementation.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class BXDomImpl implements DOMImplementation {
  /** Singleton instance. */
  private static final BXDomImpl INSTANCE = new BXDomImpl();

  /**
   * Returns the only instance of this class.
   * @return instance
   */
  public static BXDomImpl get() {
    return INSTANCE;
  }

  @Override
  public BXDoc createDocument(final String nsURI, final String qn,
      final DocumentType doctype) {
    Util.notimplemented();
    return null;
  }

  @Override
  public DocumentType createDocumentType(final String qn, final String pid,
      final String sid) {
    Util.notimplemented();
    return null;
  }

  @Override
  public Object getFeature(final String f, final String v) {
    return null;
  }

  @Override
  public boolean hasFeature(final String f, final String v) {
    return "XML".equalsIgnoreCase(f) && (v == null ||
        Token.eq(v, "", "1.0", "2.0", "3.0"));
  }
}
