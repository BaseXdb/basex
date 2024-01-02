package org.basex.io.serial;

import java.io.*;

import org.basex.query.util.ft.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This class serializes items as text.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
final class TextSerializer extends StandardSerializer {
  /**
   * Constructor, specifying serialization options.
   * @param os output stream
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  TextSerializer(final OutputStream os, final SerializerOptions sopts) throws IOException {
    super(os, sopts);
  }

  @Override
  public void serialize(final Item item) throws IOException {
    if(item instanceof XQArray) {
      for(final Item it : flatten((XQArray) item)) super.serialize(it);
    } else {
      super.serialize(item);
    }
  }

  @Override
  protected void text(final byte[] value, final FTPos ftp) throws IOException {
    out.print(Token.normalize(value, form));
    sep = false;
  }
}
