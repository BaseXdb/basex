package org.basex.index.value;

import java.io.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.util.*;

/**
 * <p>This class builds a main-memory index for attribute values and text contents.</p>
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public class MemValuesBuilder extends ValuesBuilder {
  /**
   * Constructor.
   * @param data data reference
   * @param text value type (text/attribute)
   * @param tokenize tokenizing index
   */
  public MemValuesBuilder(final Data data, final boolean text, final boolean tokenize) {
    super(data, text, tokenize);
  }

  @Override
  public MemValues build() throws IOException {
    Util.debug(det());

    final MemValues index = new MemValues(data, text, tokenize);
    for(pre = 0; pre < size; pre++) {
      if((pre & 0x0FFF) == 0) check();
      if(indexEntry() && data.textLen(pre, text) <= data.meta.maxlen) {
        index.add(data.text(pre, text), data.meta.updindex ? data.id(pre) : pre);
        count++;
      }
    }
    index.finish();
    finishIndex();
    return index;
  }

  @Override
  protected void abort() {
  }
}
