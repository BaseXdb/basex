package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.*;

/**
 * This class serializes data as XHTML.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public class XHTMLSerializer extends OutputSerializer {
  /**
   * Constructor, specifying serialization options.
   * @param os output stream reference
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  XHTMLSerializer(final OutputStream os, final SerializerOptions sopts) throws IOException {
    super(os, sopts, V10, V11);
  }

  @Override
  protected void attribute(final byte[] n, final byte[] v) throws IOException {
    // escape URI attributes
    final byte[] tagatt = concat(lc(elem), COLON, lc(n));
    final byte[] val = escape && HTMLSerializer.URIS.contains(tagatt) ? escape(v) : v;
    super.attribute(n, val);
  }

  @Override
  protected void startOpen(final byte[] t) throws IOException {
    super.startOpen(t);
    if(content && eq(lc(elem), HEAD)) ct++;
  }

  @Override
  protected void finishOpen() throws IOException {
    super.finishOpen();
    ct(false, false);
  }

  @Override
  protected void finishEmpty() throws IOException {
    if(ct(true, false)) return;
    if((html5 ? HTMLSerializer.EMPTIES5 : HTMLSerializer.EMPTIES).contains(lc(elem))) {
      print(' ');
      print(ELEM_SC);
    } else {
      print(ELEM_C);
      sep = false;
      finishClose();
    }
  }

  @Override
  protected boolean doctype(final byte[] dt) throws IOException {
    if(level != 0) return false;
    if(!super.doctype(dt) && html5) {
      if(sep) indent();
      print(DOCTYPE);
      if(dt == null) print(M_HTML);
      else print(dt);
      print(ELEM_C);
      print(nl);
    }
    return true;
  }
}
