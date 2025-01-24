package org.basex.io.serial.csv;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.parse.csv.*;
import org.basex.io.serial.*;
import org.basex.query.util.ft.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class serializes items as CSV.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class CsvW3XmlSerializer extends CsvSerializer {
  /** Names of header elements. */
  private final TokenList headers;
  /** Contents of current row. */
  private TokenList data;

  /**
   * Constructor.
   * @param os output stream
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  public CsvW3XmlSerializer(final OutputStream os, final SerializerOptions sopts)
      throws IOException {
    super(os, sopts);
    headers = header ? new TokenList() : null;
  }

  @Override
  protected void startOpen(final QNm name) {
    if(level == 2) data = new TokenList();
  }

  @Override
  protected void finishEmpty() throws IOException {
    finishOpen();
    switch(level) {
      case 2:
        if(header && elem.eq(CsvW3XmlConverter.Q_FN_COLUMN)) headers.add(EMPTY);
        break;
      case 3:
        data.add(EMPTY);
        break;
    }
    finishClose();
  }

  @Override
  protected void text(final byte[] value, final FTPos ftp) throws IOException {
    switch(level) {
      case 3:
        if(header && elem.eq(CsvW3XmlConverter.Q_FN_COLUMN)) headers.add(value);
        break;
      case 4:
        data.add(value);
        break;
    }
  }

  @Override
  protected void finishClose() throws IOException {
    if(level != 2 || !elem.eq(CsvW3XmlConverter.Q_FN_ROW)) return;
    if(header) {
      record(headers);
      header = false;
    }
    final TokenList line = data;
    record(line);
  }

  @Override
  protected void attribute(final byte[] name, final byte[] value, final boolean standalone)
      throws IOException {
    if(headers == null || !name.equals(CsvW3XmlConverter.Q_COLUMN.local())) return;
    if(data.size() < headers.size() && Token.eq(value, headers.get(data.size()))) return;
    throw CSV_SERIALIZE_X_X.getIO("Unexpected column", value);
  }
}
