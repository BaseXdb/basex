package org.basex.query.util.csv;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.file.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * <p>This class converts CSV input to XML.</p>
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class CsvParser {
  /** Separator mappings. */
  public static final byte[] SEPMAPPINGS = { ',', ';', '\t', ' ' };
  /** CSV root element. */
  private static final byte[] CSV = token("csv");
  /** CSV record element. */
  private static final byte[] RECORD = token("record");
  /** CSV field element. */
  private static final byte[] ENTRY = token("entry");
  /** CSV column attribute. */
  private static final byte[] COLUMN = token("column");

  /** Column separator (see {@link ParserProp#SEPARATOR}). */
  private final int separator;

  /** Root node. */
  private final FElem root = new FElem(CSV);
  /** Record. */
  private FElem record;

  /** Headers. */
  private final TokenList headers = new TokenList();
  /** Header flag. */
  private boolean header;
  /** Current column. */
  private int col;

  /**
   * Constructor.
   * @param sep separator character
   * @param head header flag
   */
  public CsvParser(final int sep, final boolean head) {
    separator = sep;
    header = head;
  }

  /**
   * Converts the CSV input to an XML node.
   * @param input CSV input
   * @return node
   * @throws IOException I/O exception
   */
  public FElem convert(final byte[] input) throws IOException {
    final TokenBuilder tb = new TokenBuilder();
    final NewlineInput nli = new NewlineInput(new IOContent(input));

    boolean quoted = false, open = true;
    int ch = -1;
    while(true) {
      if(ch == -1) ch = nli.read();
      if(ch == -1) break;
      if(quoted) {
        if(ch == '"') {
          ch = nli.read();
          if(ch != '"') {
            quoted = false;
            continue;
          }
        }
        tb.add(ch);
      } else if(ch == separator) {
        if(open) {
          open();
          open = false;
        }
        add(tb);
      } else if(ch == '\n') {
        finish(tb, open);
        open = true;
      } else if(ch == '"') {
        quoted = true;
      } else {
        tb.add(XMLToken.valid(ch) ? ch : '?');
      }
      ch = -1;
    }
    nli.close();

    finish(tb, open);

    return root;
  }

  /**
   * Creates a new record.
   */
  private void open() {
    if(header) return;
    record = new FElem(RECORD);
    root.add(record);
  }

  /**
   * Finishes the current record.
   * @param tb token builder
   * @param open open flag
   */
  private void finish(final TokenBuilder tb, final boolean open) {
    boolean close = !open;
    if(open && !tb.isEmpty()) {
      open();
      close = true;
    }
    add(tb);
    if(close) header = false;
    col = 0;
  }

  /**
   * Adds a field.
   * @param tb token builder
   */
  private void add(final TokenBuilder tb) {
    if(header) {
      // create element name
      final TokenBuilder name = new TokenBuilder();
      final byte[] field = tb.finish();
      for(int p = 0; p < field.length; p += cl(field, p)) {
        final int cp = cp(field, p);
        name.add((p == 0 ? XMLToken.isNCStartChar(cp) :
          XMLToken.isNCChar(cp)) ? cp : '_');
      }
      // no valid characters found: add default column name
      if(name.isEmpty()) name.add(COLUMN);

      // add header
      byte[] fb = name.finish();
      if(headers.contains(fb)) {
        int c = 2;
        do {
          fb = concat(field, token(c++));
        } while(headers.contains(fb));
      }
      headers.add(fb);
      tb.reset();
      return;
    }

    byte[] tag = headers.get(col);
    if(tag == null) tag = ENTRY;

    if(!tb.isEmpty()) {
      if(record != null) record.add(new FElem(tag).add(tb.finish()));
      tb.reset();
    }
    ++col;
  }
}
