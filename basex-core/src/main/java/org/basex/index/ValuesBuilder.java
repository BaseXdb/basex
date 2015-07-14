package org.basex.index;

import static org.basex.core.Text.*;

import org.basex.core.*;
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
   * @param options main options
   * @param text index type (text/attributes)
   */
  protected ValuesBuilder(final Data data, final MainOptions options, final boolean text) {
    super(data, options, MainOptions.INDEXSPLITSIZE,
        text ? MainOptions.TEXTINCLUDE : MainOptions.ATTRINCLUDE, text);
  }

  @Override
  protected final String det() {
    return text ? INDEX_TEXT_D : INDEX_ATTRIBUTES_D;
  }
}
