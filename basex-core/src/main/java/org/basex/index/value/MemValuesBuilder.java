package org.basex.index.value;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.util.*;

/**
 * <p>This class builds a main-memory index for attribute values and text contents.</p>
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MemValuesBuilder extends ValuesBuilder {
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
    Util.debugln(detailedInfo());

    final MemValues index = new MemValues(data, type);
    final boolean updindex = data.meta.updindex;
    final int maxlen = data.meta.maxlen;
    for(pre = 0; pre < size; pre++) {
      if((pre & 0x0FFF) == 0) check();
      if(includeNames.unit(pre)) {
        final int id = updindex ? data.id(pre) : pre;
        if(tokenize) {
          for(final byte[] token : distinctTokens(data.text(pre, text))) {
            index.add(token, id);
            count++;
          }
        } else if(data.textLen(pre, text) <= maxlen) {
          // texts of main-memory instances are references to the keys of the index
          index.add((int) data.textRef(pre), id);
          count++;
        }
      }
    }
    index.finish();
    finishIndex();
    return index;
  }
}
