package org.basex.io.serial;

import static org.basex.io.serial.SerializerOptions.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.text.*;
import java.text.Normalizer.Form;

import org.basex.core.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * This class serializes items to an output stream.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class StandardSerializer extends OutputSerializer {
  /** Separator flag. */
  protected boolean sep;
  /** Atomic flag. */
  protected boolean atomic;

  /** Normalization form. */
  private final Form form;

  /**
   * Constructor.
   * @param out print output
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  protected StandardSerializer(final PrintOutput out, final SerializerOptions sopts)
      throws IOException {

    super(out, sopts);
    indent  = sopts.yes(INDENT);

    // normalization form
    final String norm = sopts.get(NORMALIZATION_FORM);
    final Form frm;
    if(norm.equals(Text.NONE)) {
      frm = null;
    } else {
      try {
        frm = Form.valueOf(norm);
      } catch(final IllegalArgumentException ex) {
        throw SERNORM_X.getIO(norm);
      }
    }
    form = frm;
  }

  @Override
  public void reset() {
    sep = false;
    atomic = false;
  }

  // PROTECTED METHODS ============================================================================

  /**
   * Normalizes the specified text.
   * @param text text to be normalized
   * @return normalized text
   */
  protected byte[] norm(final byte[] text) {
    return form == null || ascii(text) ? text : token(Normalizer.normalize(string(text), form));
  }

  @Override
  protected void atomic(final Item item, final boolean iter) throws IOException {
    if(sep && atomic) out.print(' ');
    try {
      if(item instanceof StrStream && form == null) {
        try(final InputStream ni = ((StrStream) item).input(null)) {
          for(int cp; (cp = ni.read()) != -1;) if(iter) out.print(cp); else encode(cp);
        }
      } else {
        final byte[] str = norm(item.string(null));
        final int al = str.length;
        if(iter) {
          out.print(str);
        } else {
          for(int a = 0; a < al; a += cl(str, a)) encode(cp(str, a));
        }
      }
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
    sep = true;
    atomic = true;
  }

  /**
   * Prints the characters of the specified token.
   * @param token token
   * @throws IOException I/O exception
   */
  protected void printChars(final byte[] token) throws IOException {
    if(contains(token, '\n')) {
      final int sl = token.length;
      for(int s = 0; s < sl; s += cl(token, s)) printChar(cp(token, s));
    } else {
      out.print(token);
    }
  }

  /**
   * Writes a codepoint in the current encoding and
   * converts newlines to the operating system's default.
   * @param cp codepoint to be printed
   * @throws IOException I/O exception
   */
  protected final void printChar(final int cp) throws IOException {
    if(cp == '\n') newline();
    else out.print(cp);
  }

  /**
   * Encodes the specified codepoint before printing it.
   * @param cp codepoint to be encoded and printed
   * @throws IOException I/O exception
   */
  protected void encode(final int cp) throws IOException {
    printChar(cp);
  }
}
