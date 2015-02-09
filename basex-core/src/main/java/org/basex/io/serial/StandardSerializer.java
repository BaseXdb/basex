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
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class serializes items to an output stream.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class StandardSerializer extends OutputSerializer {
  /** Normalization form. */
  protected final Form form;
  /** WebDAV flag. */
  protected final IntObjMap<byte[]> map;

  /** Include separator. */
  protected boolean sep;
  /** Atomic flag. */
  protected boolean atomic;

  /**
   * Constructor.
   * @param out print output
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  protected StandardSerializer(final PrintOutput out, final SerializerOptions sopts)
      throws IOException {

    super(out, sopts);
    indent = sopts.yes(INDENT);
    itemsep(null);

    // normalization form
    final String norm = sopts.get(NORMALIZATION_FORM);
    if(norm.equals(Text.NONE)) {
      form = null;
    } else {
      try {
        form = Form.valueOf(norm);
      } catch(final IllegalArgumentException ex) {
        throw SERNORM_X.getIO(norm);
      }
    }

    final String maps = sopts.get(USE_CHARACTER_MAPS);
    if(maps.isEmpty()) {
      map = null;
    } else {
      map = new IntObjMap<>();
      for(final String s : Strings.split(maps, ',')) {
        final String[] kv = Strings.split(s, '=', 2);
        if(kv.length < 2) throw SERMAP_X.getIO(maps);
        map.put(kv[0].charAt(0), token(kv[1]));
      }
    }
  }

  @Override
  public void serialize(final Item item) throws IOException {
    final byte[] sp = itemsep;
    if(sp != null) {
      if(more) {
        printChars(sp);
        sep = false;
      }
    }
    super.serialize(item);
  }

  @Override
  public void reset() {
    sep = false;
    atomic = false;
    super.reset();
  }

  @Override
  protected void node(final ANode item) throws IOException {
    final Type type = item.type;
    if(type == NodeType.ATT) throw SERATTR_X.getIO(item);
    if(type == NodeType.NSP) throw SERNS_X.getIO(item);
    super.node(item);
  }

  @Override
  protected void function(final FItem item) throws IOException {
    throw SERFUNC_X.getIO(item.seqType());
  }

  // PROTECTED METHODS ============================================================================

  @Override
  protected void atomic(final Item item) throws IOException {
    if(sep && atomic) out.print(' ');
    try {
      if(item instanceof StrStream && form == null) {
        try(final InputStream ni = ((StrStream) item).input(null)) {
          for(int cp; (cp = ni.read()) != -1;) encode(cp);
        }
      } else {
        final byte[] str = norm(item.string(null));
        final int al = str.length;
        for(int a = 0; a < al; a += cl(str, a)) encode(cp(str, a));
      }
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
    sep = true;
    atomic = true;
  }

  /**
   * Normalizes the specified text.
   * @param text text to be normalized
   * @return normalized text
   */
  protected byte[] norm(final byte[] text) {
    return form == null || ascii(text) ? text : token(Normalizer.normalize(string(text), form));
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
