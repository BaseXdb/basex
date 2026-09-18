package org.basex.index.value;

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
public final class MemValuesBuilder extends IndexBuilder {
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
    final boolean updindex = data.meta.updindex, tokenize = type == IndexType.TOKEN;
    final boolean text = type == IndexType.TEXT;
    final int maxlen = data.meta.maxlen;
    for(pre = 0; pre < size; pre++) {
      if((pre & 0x0FFF) == 0) check();
      if(includeNames.unit(pre)) {
        final int id = updindex ? data.id(pre) : pre;
        if(tokenize) {
          ValueIndex.keys(data, type, pre, (key, pos) -> {
            index.add(key, id);
            count++;
          });
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
