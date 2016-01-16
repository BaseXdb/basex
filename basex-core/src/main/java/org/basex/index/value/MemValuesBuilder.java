package org.basex.index.value;

import static org.basex.util.Token.*;

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
   * @param type index type
   */
  public MemValuesBuilder(final Data data, final IndexType type) {
    super(data, type);
  }

  @Override
  public MemValues build() throws IOException {
    Util.debug(det());

    final MemValues index = new MemValues(data, type);
    final boolean updindex = data.meta.updindex && !tokenize;
    for(pre = 0; pre < size; pre++) {
      if((pre & 0x0FFF) == 0) check();
      if(indexEntry()) {
        if(tokenize) {
          for(final byte[] token : distinctTokens(data.text(pre, text))) {
            index.add(token, updindex ? data.id(pre) : pre);
            count++;
          }
        } else if(data.textLen(pre, text) <= data.meta.maxlen) {
          index.add(data.text(pre, text), updindex ? data.id(pre) : pre);
          count++;
        }
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
