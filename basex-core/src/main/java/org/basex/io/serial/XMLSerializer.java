package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * This class serializes data as XML.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class XMLSerializer extends OutputSerializer {
  /** Root elements. */
  private boolean root;
  /** Wrapper flag. */
  private final boolean wrap;

  /**
   * Constructor, specifying serialization options.
   * @param os output stream reference
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  XMLSerializer(final OutputStream os, final SerializerOptions sopts) throws IOException {
    super(os, sopts, V10, V11);

    // open results element
    wrap = wPre.length != 0;
    if(wrap) {
      openElement(concat(wPre, COLON, T_RESULTS));
      namespace(wPre, wUri);
    }
  }

  @Override
  protected void openResult() throws IOException {
    super.openResult();
    if(wrap) openElement(wPre.length == 0 ? T_RESULT : concat(wPre, COLON, T_RESULT));
  }

  @Override
  protected void closeResult() throws IOException {
    if(wrap) closeElement();
  }

  @Override
  protected void startOpen(final byte[] name) throws IOException {
    if(elems.isEmpty()) {
      if(root) check();
      root = true;
    }
    super.startOpen(name);
  }

  @Override
  protected void text(final byte[] value, final FTPos ftp) throws IOException {
    if(elems.isEmpty()) check();
    super.text(value, ftp);
  }

  @Override
  protected void atomic(final Item it, final boolean iter) throws IOException {
    if(elems.isEmpty()) check();
    super.atomic(it, iter);
  }

  @Override
  public void close() throws IOException {
    if(wrap) closeElement();
    super.close();
  }

  /**
   * Checks if document serialization is valid.
   * @throws QueryIOException query I/O exception
   */
  private void check() throws QueryIOException {
    if(!saomit) throw SERSA.getIO();
    if(docsys != null) throw SERDT.getIO();
  }
}
