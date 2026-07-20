package org.basex.io.serial;

import static org.basex.query.value.type.BasicType.*;

import java.io.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
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
    final Type type = item.type;
    try {
      if(type == DOUBLE) {
        printChars(Dbl.string(item.dbl(null)));
      } else if(type == QNAME) {
        printChar('#');
        printChars(item.string(null));
      } else if(depth == 0) {
        if(binary && item instanceof Bin) {
          try(BufferInput bi = item.input(null)) {
            for(int b; (b = bi.read()) != -1;) out.write(b);
          }
        } else {
          printChars(item.string(null));
        }
      } else {
        super.atomic(item);
      }
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
  }

  @Override
  protected Type constructor(final Type type) {
    // project mode never wraps atomic values in a type constructor
    return null;
  }

  @Override
  protected void jnode(final JNode jnode) throws IOException {
    reset();
    for(final Item item : jnode.value) {
      serialize(item);
    }
  }
}
