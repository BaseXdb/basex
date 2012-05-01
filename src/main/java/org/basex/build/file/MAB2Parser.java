package org.basex.build.file;

import static org.basex.build.file.MAB2.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.random.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class parses files in the MAB2 format
 * and sends events to the specified database builder.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class MAB2Parser extends SingleParser {
  /** Encoding of MAB2 input. */
  private static final String ENCODING = "iso-8859-1";
  /** Temporary token builder. */
  private final ByteList buffer = new ByteList();
  /** Subject assignments. */
  private final TokenMap subjects = new TokenMap();
  /** Media type assignments. */
  private final TokenMap mediatypes = new TokenMap();
  /** Language assignments. */
  private final TokenMap languages = new TokenMap();
  /** MedioVis ID assignments. */
  private final TokenMap mvids = new TokenMap();
  /** Optional lending numbers. */
  private final TokenMap lendings = new TokenMap();
  /** Optional lending status. */
  private final TokenMap status = new TokenMap();
  /** Image assignments. */
  private final TokenMap posters = new TokenMap();
  /** Genre assignments. */
  private final TokenMap genres = new TokenMap();

  /** Temporary build data. */
  private final byte[][] sig = new byte[500][];
  /** Temporary build data. */
  private final byte[][] auth = new byte[50][];
  /** Temporary build data. */
  private final byte[][] inst = new byte[50][];

  /** Flat database creation. */
  private final boolean flat;
  /** Input to be parsed. */
  private final DataAccess input;

  /** Temporary read position. */
  private long off;
  /** Maximum id. */
  private int maxid;

  /**
   * Constructor.
   * @param source source data
   * @param pr database properties
   * @throws IOException I/O exception
   */
  public MAB2Parser(final IO source, final Prop pr) throws IOException {
    super(source, pr);
    // set parser properties
    final ParserProp props = new ParserProp(pr.get(Prop.PARSEROPT));
    flat = props.is(ParserProp.FLAT);
    input = new DataAccess(new IOFile(source.path()));
  }

  @Override
  public void parse() throws IOException {
    // read in indexes
    index(mediatypes, "mediatypes");
    index(subjects, "subjects");
    index(languages, "lang");
    index(mvids, "mvids");
    index(lendings, "lendings");
    index(status, "status");
    index(posters, "posters");
    index(genres, "genres");

    // find maximum id
    for(int i = 1; i <= mvids.size(); ++i) {
      final int id = toInt(mvids.value(i));
      if(maxid < id) maxid = id;
    }

    // check beginning of input file
    if(input.read1() != '#' || input.read1() != '#' || input.read1() != '#') {
      throw new BuildException("Invalid MAB2 input (doesn't start with ###)");
    }

    builder.startElem(LIBRARY, atts.reset());

    // find file offsets of all titles
    final Performance p = new Performance();

    // MAB2 entries
    final TokenObjMap<MAB2Entry> ids = new TokenObjMap<MAB2Entry>();

    int i = 0;
    for(byte[] id; (id = id(input)) != null;) {
      final long pos = off;
      final byte[] par = par(input);

      final boolean child = par != null;
      final byte[] key = child ? par : id;
      MAB2Entry entry = ids.get(key);
      if(entry == null) {
        entry = new MAB2Entry();
        ids.add(key, entry);
      }
      if(child) entry.add(pos);
      else entry.pos(pos);

      if(Prop.debug) {
        if((++i & 0x7FFF) == 0) Util.err(" " + i + '\n');
        else if((i & 0xFFF) == 0) Util.err("!");
        else if((i & 0x3FF) == 0) Util.err(".");
      }
    }

    if(Prop.debug) Util.err("\nParse Offsets (%): %/%\n", ids.size(), p,
        Performance.getMemory());

    // create all titles
    for(i = 1; i <= ids.size(); ++i) {
      final MAB2Entry entry = ids.value(i);
      final long pos = entry.pos;
      // check if top entry exists...
      final byte[] l = pos != 0 ? addEntry(input, pos, entry.size, null) : null;
      // loop through all children...
      for(int j = 0; j < entry.size; ++j) {
        addEntry(input, entry.children[j], 0, l);
      }
      if(entry.size != 0 && pos != 0 && !flat) builder.endElem();
    }
    if(Prop.debug) Util.err("\nCreate Titles: %/%\n", p, Performance.getMemory());
    builder.endElem();

    // write the mediovis ids back to disk
    final PrintOutput out = new PrintOutput("mvids.dat");
    for(i = 1; i <= mvids.size(); ++i) {
      out.print(mvids.key(i));
      out.write('\t');
      out.println(mvids.value(i));
    }
    out.close();
  }

  @Override
  public void close() throws IOException {
    input.close();
  }

  /**
   * Returns the next id.
   * @param in input stream
   * @return id
   */
  private byte[] id(final DataAccess in) {
    while(in.more()) {
      if(in.read1() != '\n') continue;
      final int n = in.read1();
      if(n == '0' && in.read1() == '0' && in.read1() == '1') {
        off = in.cursor() - 3;
        return ident(in);
      }
    }
    return null;
  }

  /**
   * Returns the next parent id.
   * @param in input stream
   * @return id
   */
  private static byte[] par(final DataAccess in) {
    while(in.more()) {
      if(in.read1() != '\n') continue;
      final int b1 = in.read1();
      if(b1 == '#' || b1 == '\n') return null;
      if(b1 == '0' && in.read1() == '1' && in.read1() == '0' ||
         b1 == '4' && in.read1() == '5' && in.read1() == '3') return ident(in);
    }
    return null;
  }

  /**
   * Returns the next text.
   * @param in input stream
   * @return next text
   */
  private static byte[] ident(final DataAccess in) {
    in.read1();
    int l = 0;
    for(byte b; (b = in.read1()) >= ' ';) CACHE[l++] = b;
    return Arrays.copyOf(CACHE, l);
  }

  /** Buffer. */
  private static final byte[] CACHE = new byte[16];

  /**
   * Gets all characters up to specified character.
   * @param in input stream
   * @param delim delimiter
   * @return byte array
   */
  private byte[] find(final DataAccess in, final byte delim) {
    buffer.reset();
    while(in.more()) {
      final byte c = in.read1();
      if(c == delim) return buffer.toArray();
      if(c < 0 || c >= ' ') buffer.add(c);
    }
    return null;
  }

  /**
   * Adds an entry.
   * @param in input stream
   * @param pos file offset to start from
   * @param sb number of subordinate titles
   * @param last last title
   * @return last title
   * @throws IOException I/O exception
   */
  private byte[] addEntry(final DataAccess in, final long pos, final int sb,
      final byte[] last) throws IOException {

    /* Temporary build data. */
    byte[] mvID = null;
    /* Temporary build data. */
    byte[] bibID = null;
    /* Temporary build data. */
    byte[] title = null;
    /* Temporary build data. */
    byte[] description = null;
    /* Temporary build data. */
    byte[] type = null;
    /* Temporary build data. */
    byte[] language = null;
    /* Temporary build data. */
    byte[] original = null;
    /* Temporary build data. */
    byte[] subtitle = null;
    /* Temporary build data. */
    byte[] town = null;
    /* Temporary build data. */
    byte[] publisher = null;
    /* Temporary build data. */
    byte[] year = null;
    /* Temporary build data. */
    byte[] format = null;
    /* Temporary build data. */
    byte[] details = null;
    /* Temporary build data. */
    byte[] note = null;
    /* Temporary build data. */
    byte[] isbn = null;
    /* Temporary build data. */
    byte[] subject = null;
    /* Temporary build data. */
    int nrSigs = 0;
    /* Temporary build data. */
    int nrAuth = 0;
    /* Temporary build data. */
    int nrInst = 0;
    /* Temporary build data. */
    boolean shortTitle = false;

    // position disk cursor
    in.cursor(pos);

    // collect meta-data
    while(true) {
      final byte[] line = find(in, (byte) '\n');
      final int l = line.length;

      if(l > 3) {
        if(line[0] == '#') continue;

        final int n = toInt(line, 0, 3);
        if(n == 1) {
          if(bibID == null) {
            bibID = string(line);
            mvID = mvids.get(bibID);
            if(mvID == null) {
              mvID = token(++maxid);
              mvids.add(bibID, mvID);
            }
          }
        } else if(n == 29) {
          type = mediatypes.get(num(line));
        } else if(n == 37 && language == null) {
          language = language(line);
        } else if(n == 81) {
          title = string(line);
          shortTitle = true;
        } else if(n >= 100 && n < 200 && (n & 3) == 0) {
          auth[nrAuth++] = string(line);
        } else if(n >= 200 && n < 300 && (n & 3) == 0) {
          inst[nrInst++] = string(line);
        } else if(n == 304) {
          original = string(line);
        } else if(n == 310) {
          title = string(line);
          shortTitle = true;
        } else if(n == 331) {
          if(title == null) title = string(line);
          else if(shortTitle) description = string(line);
        } else if(n == 335) {
          subtitle = string(line);
        } else if(n == 340) {
          if(original == null) original = string(line);
        } else if(n == 359) {
          description = merge(description, string(line));
        } else if(n == 410) {
          town = string(line);
        } else if(n == 412) {
          publisher = string(line);
        } else if(n == 425) {
          year = year(line);
        } else if(n == 433) {
          format = string(line);
        } else if(n == 501) {
          details = string(line);
          year = year2(details, year);
        } else if(n == 537) {
          note = string(line);
        } else if(n == 540) {
          isbn = string(line);
        } else if(n == 542) {
          isbn = string(line);
        } else if(n == 544) {
          sig[nrSigs++] = string(line);
        } else if(n == 700) {
          if(nrSigs == 0) sig[nrSigs++] = string(line);
        }
      } else {
        atts.reset();
        atts.add(MV_ID, mvID);
        atts.add(BIB_ID, bibID);
        if(sb != 0 && !flat) atts.add(MAX, token(sb));

        // merge super and sub titles
        if(last != null) {
          if(title == null) title = last;
          else if(!eq(last, title)) title = concat(last, SEMI, title);
        }

        // add line below to omit root nodes
        builder.startElem(MEDIUM, atts);
        add(TYPE, type);
        add(LANGUAGE, language);
        for(int s = 0; s < nrAuth; ++s) add(AUTHOR, auth[s]);
        for(int s = 0; s < nrInst; ++s) add(INSTITUTE, inst[s]);
        add(ORIGINAL, original);
        add(TITLE, title);
        add(SUBTITLE, subtitle);
        add(DESCRIPTION, description);
        add(TOWN, town);
        add(PUBLISHER, publisher);
        add(YEAR, year);
        add(FORMAT, format);
        add(DETAILS, details);
        add(NOTE, note);
        for(int s = 0; s < nrSigs; ++s) add(SIGNATURE, sig[s]);
        // actually: several subjects/lending numbers per medium..
        for(int s = 0; s < nrSigs; ++s) {
          if(subject == null) subject = subjects.get(subject(sig[s]));
        }
        add(SUBJECT, subject);
        add(ISBN, isbn);
        add(POSTER, posters.get(bibID));
        add(GENRE, genres.get(mvID));
        add(STATUS, status.get(bibID));
        add(LENDINGS, lendings.get(bibID));
        if(sb == 0 || flat) builder.endElem();
        return title;
      }
    }
  }

  /**
   * Adds a tag and a content node.
   * @param tag tag to be added
   * @param cont content to be added
   * @throws IOException I/O exception
   */
  private void add(final byte[] tag, final byte[] cont) throws IOException {
    if(cont == null) return;
    builder.startElem(tag, atts.reset());
    builder.text(utf8(cont, ENCODING));
    builder.endElem();
  }

  /**
   * Parses and returns a year.
   * @param line line to be parsed
   * @return byte array
   */
  private static byte[] year(final byte[] line) {
    final byte[] n = new byte[4];
    final int l = line.length;
    int c = 0;
    for(int i = 4; i < l; ++i) {
      final byte b = line[i];
      if(b >= '0' && b <= '9') {
        n[c++] = b;
        if(c == 4) return n;
      }
    }
    return c != 0 ? Arrays.copyOf(n, c) : null;
  }

  /**
   * Looks up and returns four digits in the specified line
   * If no digits are found, the specified year is returned.
   * @param line line to be parsed
   * @param yr year
   * @return year
   */
  private static byte[] year2(final byte[] line, final byte[] yr) {
    final int l = line.length;
    int i = -1;
    int j = -1;
    while(++i != l) {
      final byte b = line[i];
      if(b < '0' || b > '9') {
        if(i - 5 == j) break;
        j = i;
      }
    }
    if(i - 5 != j) return yr;

    int oy = yr != null ? toInt(yr) : 0;
    if(oy >= 1400 && oy <= 1950) return yr;

    final byte[] y = Arrays.copyOfRange(line, j + 1, j + 5);
    oy = toInt(y);
    return oy >= 1500 && oy <= 2050 ? y : yr;
  }

  /**
   * Parses and returns a subject.
   * @param line line to be parsed
   * @return byte array
   */
  private static byte[] subject(final byte[] line) {
    final byte[] n = new byte[3];
    int i = -1;
    final int l = line.length;
    while(++i != l && line[i] < 'a');
    int c = 0;
    while(i != l && line[i] >= 'a') {
      n[c++] = line[i++];
      if(c == 3) return n;
    }
    return null;
  }

  /**
   * Corrects special characters in the language attribute.
   * @param token token to be corrected
   * @return corrected characters
   */
  private byte[] language(final byte[] token) {
    final byte[] t = string(token);
    for(int i = 0; i < t.length; ++i) if(t[i] == '?' || t[i] == '$') t[i] = '+';
    final TokenBuilder tb = new TokenBuilder();
    for(final byte[] lang : split(t, '+')) {
      final byte[] l = languages.get(lang);
      if(!tb.isEmpty()) tb.add('+');
      tb.add(l != null ? l : t);
    }
    return tb.finish();
  }

  /**
   * Replaces some characters in the specified line.
   * @param line line to be modified
   * @return modified byte array
   */
  private static byte[] string(final byte[] line) {
    final byte[] tmp = new byte[line.length - 4];
    int c = 0;
    final int l = line.length;
    boolean space = false;
    for(int s = 4; s < l; ++s) {
      byte b = line[s];
      // double cross
      if(b == -121) b = '+';
      // delimiter
      else if(b == -84) b = ' ';
      else if(b == '<') b = '[';
      else if(b == '>') b = ']';
      if(b == ' ' && (space || s == 4)) continue;
      space = b == ' ';
      tmp[c++] = b;
    }
    return c == tmp.length ? tmp : Arrays.copyOf(tmp, c);
  }

  /**
   * Replaces some characters in the specified line.
   * @param line line to be modified
   * @return modified byte array
   */
  private static byte[] num(final byte[] line) {
    final int l = line.length;
    int s = 3;
    while(++s < l && line[s] == '0');
    return Arrays.copyOfRange(line, s, line.length);
  }

  /**
   * Merges two byte arrays.
   * @param text1 first text
   * @param text2 second text
   * @return byte array
   */
  private static byte[] merge(final byte[] text1, final byte[] text2) {
    return text1 == null ? text2 : concat(text1, token(". "), text2);
  }

  /**
   * Fills the specified hash with the file input.
   * @param hash hash to be filled
   * @param fn file to be read
   */
  private void index(final TokenMap hash, final String fn) {
    try {
      final DataAccess in = new DataAccess(new IOFile(fn + ".dat"));
      while(true) {
        final byte[] key = find(in, (byte) '\t');
        final byte[] val = find(in, (byte) '\n');
        if(key == null) break;
        hash.add(key, val);
      }
    } catch(final IOException ex) {
      Util.debug(ex);
    }
  }

  /**
   * This is a simple data structure for storing MAB2 entries.
   *
   * @author BaseX Team 2005-12, BSD License
   * @author Christian Gruen
   */
  static final class MAB2Entry {
    /** Children offsets. */
    long[] children;
    /** File offset; 0 if no parent node exists. */
    long pos;
    /** Number of children. */
    int size;

    /**
     * Adds a child.
     * @param c child to be added
     */
    void add(final long c) {
      if(children == null) children = new long[1];
      else if(size == children.length)
        children = Arrays.copyOf(children, size << 1);
      children[size++] = c;
    }

    /**
     * Sets the file offset.
     * @param p file offset
     */
    void pos(final long p) {
      pos = p;
    }
  }
}
