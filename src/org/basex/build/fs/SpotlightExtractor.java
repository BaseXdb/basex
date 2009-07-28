/**
 *
 */
package org.basex.build.fs;

import static org.basex.util.Token.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.basex.BaseX;
import org.basex.build.fs.parser.Metadata;
import org.basex.build.fs.parser.ParserUtil;
import org.basex.build.fs.parser.Metadata.DateField;
import org.basex.build.fs.parser.Metadata.StringField;
import org.basex.util.Array;
import org.basex.util.Token;

import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Extracts metadata from Apple's Spotlight.
 * @author Bastian Lemke
 */
@SuppressWarnings("all")
public class SpotlightExtractor {
  // [BL] access spotlight API via JNI

  private final ProcessBuilder pb;
  private static final String MDLS = "/usr/bin/mdls";
  private static final String RAW_FLAG = "-raw";
  private static final String NAME_FLAG = "-name";
  private static final byte[] NULL_ITEM = { '(', 'n', 'u', 'l', 'l', ')'};

  private final List<String> commands;
  private final int pathPos;
  private byte[] data;
  final NewFSParser parser;
  final Metadata meta;

  /** The format of the date values. */
  private static final SimpleDateFormat SDF = new SimpleDateFormat(
      "yyyy-MM-dd HH:mm:ss Z");

  /**
   * Registered metadata items and corresponding actions for metadata events.
   */
  enum Item {
    AttributeChangeDate {
      @Override
      void parse(final SpotlightExtractor obj, final byte[] d)
          throws IOException {
        obj.dateEvent(DateField.DATE_ATTRIBUTE_MODIFIED, d);
      }
    },
    Authors {
      @Override
      void parse(final SpotlightExtractor obj, final byte[] d)
          throws IOException {
        obj.meta.setString(StringField.CREATOR, d);
        obj.parser.metaEvent(obj.meta);
      }
    },
    City {
      @Override
      void parse(final SpotlightExtractor obj, final byte[] d)
          throws IOException {
        obj.meta.setString(StringField.SPATIAL, d);
        obj.parser.metaEvent(obj.meta);
      }
    },
    FSContentChangeDate {
      @Override
      void parse(final SpotlightExtractor obj, final byte[] d)
          throws IOException {
        obj.dateEvent(DateField.DATE_CONTENT_MODIFIED, d);
      }
    },
    FSOwnerGroupID {
      @Override
      void parse(final SpotlightExtractor obj, final byte[] d)
          throws IOException { /* */}
    };

    /**
     * Parses the data and fires parser events.
     * @param obj the {@link SpotlightExtractor} object to fire events from.
     * @param d the data to parse.
     * @throws IOException if any error occurs while writing to the parser.
     */
    abstract void parse(final SpotlightExtractor obj, final byte[] d)
        throws IOException;

    @Override
    public String toString() {
      return "kMDItem" + name();
    }
  }

  /**
   * Converts the date to the correct xml format and fires an event.
   * @param field the {@link DateField}
   * @param date the date to convert.
   * @throws IOException if any error occurs while writing to the parser.
   */
  void dateEvent(final DateField field, final byte[] date) throws IOException {
    XMLGregorianCalendar gcal = null;
    try {
      gcal = ParserUtil.convertDate(SDF.parse(string(date)));
    } catch(ParseException e) {
      if(NewFSParser.VERBOSE) BaseX.debug("Failed to convert date (%)",
          string(date));
    }
    if(gcal == null) return;
    meta.setDate(field, gcal);
    parser.metaEvent(meta);
  }

  /**
   * Initializes the spotlight extractor.
   * @param fsParser the parser instance to fire events.
   */
  public SpotlightExtractor(final NewFSParser fsParser) {
    parser = fsParser;
    meta = new Metadata();
    commands = new ArrayList<String>();
    commands.add(MDLS);
    /* -name and -raw don't work properly (items are in wrong order) :-( */
    // for(Item i : Item.values()) {
    // commands.add(NAME_FLAG);
    // commands.add(i.toString());
    // }
    // commands.add(RAW_FLAG);
    pathPos = commands.size();
    commands.add(""); // dummy command for file path
    pb = new ProcessBuilder(commands);
  }

  /**
   * Queries spotlight for metadata items for the file and fires parser events.
   * @param file the file to search metadata for.
   * @throws IOException if any error occurs...
   */
  public void parse(final File file) throws IOException {
    data = new byte[8192];
    commands.set(pathPos, file.getAbsolutePath());
    Process p = pb.start();

    InputStream in = p.getInputStream();
    int bytesRead = 0;
    int status;
    while(true) {
      status = in.read(data, bytesRead, data.length - bytesRead);
      if(status == -1) break;
      bytesRead += status;
      if(bytesRead == data.length) {
        data = Array.resize(data, data.length, bytesRead << 1);
      }
    }
    byte[][] tokens = Token.split(data, 10); // 10 -> LF char => line by line
    int numLines = tokens.length;
    for(int i = 0; i < numLines; i++) {
      byte[] line = tokens[i];
      if(ws(line)) continue;
      int len = line.length;
      if(!startsWith(line, new byte[] { 'k', 'M', 'D', 'I', 't', 'e', 'm'})) {
        continue;
      }
      int pos = indexOf(line, ' ');
      if(pos == -1) {
        BaseX.debug("Failed to parse spotlight items (%)", file);
        return;
      }
      String itemName = string(substring(line, 7, pos));
      Item item;
      try {
        item = Item.valueOf(itemName);
      } catch(IllegalArgumentException e) { // not registered
        continue;
      }
      for(; pos < len; pos++)
        if(line[pos] == '=') break;
      pos += 2;
      if(pos >= len) {
        BaseX.debug("Failed to parse spotlight items (%)", file);
        return;
      }
      if(line[pos] == '(') {
        while(tokens[++i][0] != ')') {
          line = trim(tokens[i]);
          if(line[0] == '"') line = substring(line, 1, line.length - 1);
          item.parse(this, line);
        }
      } else {
        int end = line.length;
        if(line[pos] == '"') {
          pos++;
          end--;
        }
        item.parse(this, substring(line, pos, end));
      }
    }
  }
}
