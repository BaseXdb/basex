package org.basex.api.dom;

import org.basex.util.*;
import org.w3c.dom.*;

/**
 * DOM - implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class BXDomImpl implements DOMImplementation {
  /** Singleton instance. */
  private static final BXDomImpl INSTANCE = new BXDomImpl();

  /**
   * Returns the only instance of this class.
   * @return instance
   */
  static BXDomImpl get() {
    return INSTANCE;
  }

  @Override
  public BXDoc createDocument(final String uri, final String name, final DocumentType type) {
    throw BXNode.notImplemented();
  }

  @Override
  public DocumentType createDocumentType(final String name, final String pid, final String sid) {
    throw BXNode.notImplemented();
  }

  @Override
  public Object getFeature(final String name, final String value) {
    return null;
  }

  @Override
  public boolean hasFeature(final String name, final String value) {
    return "XML".equalsIgnoreCase(name) && (value == null ||
        Strings.eq(value, "", "1.0", "2.0", "3.0"));
  }
}
