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
   * Converts the specified input to an XML element.
   * @param input CSV input
   * @return node
   * @throws IOException I/O exception
   */
  public FElem convert(final byte[] input) throws IOException {
    return convert(new NewlineInput(new IOContent(input)));
  }

  /**
   * Converts the specified input stream to an XML element.
   * @param input CSV input
   * @return node
   * @throws IOException I/O exception
   */
  public FElem convert(final NewlineInput input) throws IOException {
    final TokenBuilder data = new TokenBuilder();
    boolean quoted = false, open = true;
    int ch = -1;

    try {
      while(true) {
        if(ch == -1) ch = input.read();
        if(ch == -1) break;
        if(quoted) {
          if(ch == '"') {
            ch = input.read();
            if(ch != '"') {
              quoted = false;
              continue;
            }
          }
          data.add(ch);
        } else if(ch == separator) {
          if(open) {
            newRecord();
            open = false;
          }
          newEntry(data);
        } else if(ch == '\n') {
          finish(data, open);
          open = true;
        } else if(ch == '"') {
          quoted = true;
        } else {
          data.add(XMLToken.valid(ch) ? ch : '?');
        }
        ch = -1;
      }
    } finally {
      input.close();
    }

    finish(data, open);
    return root;
  }

  /**
   * Creates a new record.
   */
  private void newRecord() {
    if(header) return;
    record = new FElem(RECORD);
    root.add(record);
  }

  /**
   * Finishes the current record.
   * @param entry current entry
   * @param open open flag
   */
  private void finish(final TokenBuilder entry, final boolean open) {
    if(open && !entry.isEmpty()) newRecord();
    if(!entry.isEmpty()) newEntry(entry);
    header = false;
    col = 0;
  }

  /**
   * Adds an entry.
   * @param entry current entry
   */
  private void newEntry(final TokenBuilder entry) {
    if(header) {
      // add header
      headers.add(XMLToken.encode(entry.finish()));
      entry.reset();
    } else {
      byte[] tag = headers.get(col);
      if(tag == null) tag = ENTRY;

      if(record != null) record.add(new FElem(tag).add(entry.finish()));
      entry.reset();
      ++col;
    }
  }
}
