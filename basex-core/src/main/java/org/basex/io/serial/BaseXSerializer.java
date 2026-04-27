package org.basex.io.serial;

import static org.basex.query.value.type.BasicType.*;

import java.io.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;

/**
 * This class serializes items in a project-specific mode.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BaseXSerializer extends AdaptiveSerializer {
  /** Binary. */
  private final boolean binary;
  /** Level counter. */
  private int nested;

  /**
   * Constructor, specifying serialization options.
   * @param os output stream
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  BaseXSerializer(final OutputStream os, final SerializerOptions sopts) throws IOException {
    super(os, sopts, false);
    binary = sopts.yes(SerializerOptions.BINARY);
  }

  @Override
  protected void atomic(final Item item) throws IOException {
    if(nested == 0) {
      try {
        if(binary && item instanceof Bin) {
          try(BufferInput bi = item.input(null)) {
            for(int b; (b = bi.read()) != -1;) out.write(b);
          }
        } else if(item.type == QNAME) {
          printChar('#');
          printChars(((QNm) item).prefixId());
        } else {
          printChars(item.string(null));
        }
      } catch(final QueryException ex) {
        throw new QueryIOException(ex);
      }
    } else {
      super.atomic(item);
    }
  }

  @Override
  protected void array(final XQArray item) throws IOException {
    ++nested;
    super.array(item);
    --nested;
  }

  @Override
  protected void map(final XQMap item) throws IOException {
    ++nested;
    super.map(item);
    --nested;
  }

  @Override
  protected void jnode(final JNode jnode) throws IOException {
    // root: serialize the wrapped value; non-root: keep the key by wrapping the node in its
    // container (map entry → single-entry map, array member → single-member array, index dropped)
    if(jnode.isRoot()) {
      reset();
      for(final Item item : jnode.value) {
        serialize(item);
      }
    } else if(jnode.container() instanceof XQArray) {
      array(XQArray.get(jnode.value));
    } else {
      map(XQMap.get(jnode.key, jnode.value));
    }
  }
}
