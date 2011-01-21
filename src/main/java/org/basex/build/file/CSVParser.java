package org.basex.build.file;

import static org.basex.util.Token.*;
import static org.basex.data.DataText.*;
import java.io.IOException;
import java.util.HashMap;
import org.basex.build.FileParser;
import org.basex.io.BufferInput;
import org.basex.io.IO;
import org.basex.util.TokenBuilder;

/**
 * This class parses files in the CSV format
 * and sends events to the specified database builder.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class CSVParser extends FileParser {
  /** CSV root element. */
  private static final byte[] CSV = token("csv");
  /** CSV record element. */
  private static final byte[] RECORD = token("record");
  /** CSV field element. */
  private static final byte[] FIELD = token("field");
  /** CSV row attribute. */
  private static final byte[] ROW = token("row");
  /** CSV column attribute. */
  private static final byte[] COLUMN = token("col");

  /** Separator. */
  private final char separator = ',';
  /** Encoding. */
  private final String encoding = UTF8;

  /** Enumerate columns. */
  private final boolean cols;
  /** Add attributes. */
  private final boolean attr;
  
  /**
   * Constructor.
   * @param path file path
   * @param ta database target
   * @param props database properties
   */
  public CSVParser(final IO path, final String ta,
      final HashMap<String, String> props) {

    super(path, ta);
    cols = YES.equalsIgnoreCase(props.get("enumerate"));
    attr = !NO.equalsIgnoreCase(props.get("attributes"));
  }

  @Override
  public void parse() throws IOException {
    builder.startElem(CSV, atts);

    boolean quoted = false;
    boolean nl = true;
    final TokenBuilder tb = new TokenBuilder();

    final BufferInput bi = new BufferInput(file.path());
    bi.encoding(encoding);

    int r = 1;
    int c = 1;
    int ch = 0;
    while(true) {
      if(ch == 0) ch = bi.readChar();
      if(ch == 0) break;

      if(quoted) {
        if(ch == '"') {
          ch = bi.readChar();
          if(ch != '"') {
            quoted = false;
            continue;
          }
        }
        if(ch != 0x0D) tb.add(ch);
      } else {
        // separator
        if(ch == separator) {
          if(nl) {
            record(r++);
            nl = false;
          }
          field(tb, c++);
        } else if(ch == 0x0A) {
          field(tb, c++);
          if(!nl) {
            builder.endElem(RECORD);
            nl = true;
          }
          c = 1;
        } else if(ch == '"') {
          quoted = true;
        } else if(ch != 0x0D) {
          tb.add(ch);
        }
      }
      ch = 0;
    }
    bi.close();

    if(!nl) builder.endElem(RECORD);
    builder.endElem(CSV);
  }

  /**
   * Adds a record to the database.
   * @param r row
   * @throws IOException I/O exception
   */
  private void record(final int r) throws IOException {
    if(attr) {
      atts.reset();
      atts.add(ROW, token(r));
    }
    builder.startElem(RECORD, atts);
  }

  /**
   * Adds a field to the database.
   * @param tb token builder
   * @param c column
   * @throws IOException I/O exception
   */
  private void field(final TokenBuilder tb, final int c) throws IOException {
    if(attr) {
      atts.reset();
      atts.add(COLUMN, token(c));
    }
    final byte[] tag = cols ? concat(COLUMN, token(c)) : FIELD;
    builder.startElem(tag, atts);
    builder.text(tb);
    builder.endElem(tag);
    tb.reset();
  }
}
