package org.basex.api.dom;

import org.basex.core.Main;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DocumentType;

/**
 * DOM - implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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

  public BXDoc createDocument(final String nsURI, final String qn,
      final DocumentType doctype) {
    Main.notimplemented();
    return null;
  }

  public DocumentType createDocumentType(final String qn, final String pid,
      final String sid) {
    Main.notimplemented();
    return null;
  }

  public Object getFeature(final String f, final String v) {
    return null;
  }

  public boolean hasFeature(final String f, final String v) {
    return "XML".equalsIgnoreCase(f) && (v == null || v.isEmpty() ||
        v.equals("1.0") || v.equals("2.0") || v.equals("3.0"));
  }
}
