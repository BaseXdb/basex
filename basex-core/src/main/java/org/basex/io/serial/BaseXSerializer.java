package org.basex.io.serial;

import static org.basex.query.value.type.BasicType.*;

import java.io.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;

/**
 * This class serializes items in a project-specific mode.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BaseXSerializer extends AdaptiveSerializer {
  /** Binary. */
  private final boolean binary;

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
    if(depth == 0) {
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
  protected Type constructor(final Type type) {
    return null;
  }

  @Override
  protected void jnode(final JNode jnode) throws IOException {
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
