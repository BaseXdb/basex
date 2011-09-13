package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class serializes data as XHTML.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class XHTMLSerializer extends OutputSerializer {
  /**
   * Constructor, specifying serialization options.
   * @param os output stream reference
   * @param p serialization properties
   * @throws IOException I/O exception
   */
  XHTMLSerializer(final OutputStream os, final SerializerProp p)
      throws IOException {
    super(os, p, V10, V11);
  }

  @Override
  public void attribute(final byte[] n, final byte[] v) throws IOException {
    // escape URI attributes
    final byte[] tagatt = concat(lc(tag), COLON, lc(n));
    final byte[] val = escape && URIS.id(tagatt) != 0 ? escape(v) : v;
    super.attribute(n, val);
  }

  @Override
  protected void startOpen(final byte[] t) throws IOException {
    super.startOpen(t);
    if(content && eq(lc(tag), HEAD)) ct++;
  }

  @Override
  protected void finishOpen() throws IOException {
    super.finishOpen();
    if(ct(false, false)) return;
  }

  @Override
  protected void finishEmpty() throws IOException {
    if(ct(true, false)) return;
    if(EMPTIES.contains(lc(tag))) {
      print(' ');
      print(ELEM_SC);
    } else {
      print(ELEM_C);
      ind = false;
      finishClose();
    }
  }
}
