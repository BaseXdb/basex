package org.basex.index;

import static org.basex.core.Text.*;

import org.basex.data.*;

/**
 * Builder for values-based index structures.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class ValuesBuilder extends IndexBuilder {
  /**
   * Constructor.
   * @param data reference
   * @param text index type (text/attributes)
   */
  protected ValuesBuilder(final Data data, final boolean text) {
    super(data, data.meta.splitsize, text ? data.meta.textinclude : data.meta.attrinclude, text);
  }

  @Override
  protected final String det() {
    return text ? INDEX_TEXT_D : INDEX_ATTRIBUTES_D;
  }
}
