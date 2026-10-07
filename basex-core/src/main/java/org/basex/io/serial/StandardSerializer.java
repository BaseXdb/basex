package org.basex.io.serial;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;

/**
 * This class serializes items to an output stream.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class StandardSerializer extends OutputSerializer {
  /** Include separator. */
  protected boolean sep;
  /** Atomic flag. */
  protected boolean atomic;

  /**
   * Constructor.
   * @param os output stream
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  protected StandardSerializer(final OutputStream os, final SerializerOptions sopts)
      throws IOException {
    super(os, sopts);
  }

  @Override
  protected boolean separate() throws IOException {
    if(!more || itemsep == null) return false;
    // separators are inserted as text nodes: characters are mapped, normalized and escaped
    expand(itemsep, this::printUnmapped);
    return true;
  }

  @Override
  protected void item(final Item item) throws IOException {
    if(separate()) sep = false;
    super.item(item);
  }

  @Override
  public void reset() {
    sep = false;
    atomic = false;
    super.reset();
  }

  @Override
  protected void node(final XNode node) throws IOException {
    final Kind kind = node.kind();
    if(kind == Kind.ATTRIBUTE) throw SERATTR_X.getIO(node);
    if(kind == Kind.NAMESPACE) throw SERNS_X.getIO(node);
    super.node(node);
  }

  @Override
  protected void function(final FItem item) throws IOException {
    if(!(item instanceof final XQArray array)) throw SERFUNC_X.getIO(item.seqType());
    for(final Value value : array.members()) {
      for(final Item it : value) item(it);
    }
  }

  @Override
  protected void atomic(final Item item) throws IOException {
    if(sep && atomic) out.print(' ');
    try {
      if(item instanceof StrLazy && form == null) {
        try(TextInput ti = item.stringInput(null)) {
          for(int cp; (cp = ti.read()) != -1;) printChar(cp);
        }
      } else {
        expand(item.string(null), this::printUnmapped);
      }
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
    sep = true;
    atomic = true;
  }

  @Override
  protected final void printChar(final int cp) throws IOException {
    final byte[] value = cmap != null ? cmap.get(cp) : null;
    if(value != null) out.print(value);
    else print(cp);
  }

  /**
   * Prints a single character.
   * @param cp codepoint
   * @throws IOException I/O exception
   */
  protected void print(final int cp) throws IOException {
    out.print(cp);
  }

  /**
   * Prints characters that were not mapped.
   * @param text characters
   * @throws IOException I/O exception
   */
  protected final void printUnmapped(final byte[] text) throws IOException {
    final int tl = text.length;
    for(int t = 0; t < tl; t += cl(text, t)) print(cp(text, t));
  }

  /**
   * Flattens an array.
   * @param array array
   * @return contained items
   */
  static ItemList flatten(final XQArray array) {
    final ItemList list = new ItemList();
    for(final Value value : array.members()) {
      for(final Item item : value) {
        if(item instanceof final XQArray arr) {
          list.add(flatten(arr));
        } else {
          list.add(item);
        }
      }
    }
    return list;
  }
}
