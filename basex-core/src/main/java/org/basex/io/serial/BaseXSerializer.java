package org.basex.io.serial;

import static org.basex.query.value.type.AtomType.*;

import java.io.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
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
        } else if(item.type == AtomType.DOUBLE) {
          printChars(Dbl.string(item.dbl(null)));
        } else {
          if(item.type == QNAME) printChar('#');
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
}
