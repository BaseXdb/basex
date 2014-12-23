package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.*;

/**
 * This class serializes data as XHTML.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class XHTMLSerializer extends OutputSerializer {
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
  protected void attribute(final byte[] name, final byte[] value) throws IOException {
    // escape URI attributes
    final byte[] nm = concat(lc(elem), COLON, lc(name));
    final byte[] val = escuri && HTMLSerializer.URIS.contains(nm) ? escape(value) : value;
    super.attribute(name, val);
  }

  @Override
  protected void startOpen(final byte[] value) throws IOException {
    super.startOpen(value);
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
  protected boolean doctype(final byte[] type) throws IOException {
    if(lvl != 0) return false;
    if(!super.doctype(type) && html5) {
      if(sep) indent();
      print(DOCTYPE);
      if(type == null) print(HTML);
      else print(type);
      print(ELEM_C);
      print(nl);
    }
    return true;
  }
}
