package org.basex.build.mediovis;

import static org.basex.build.mediovis.MAB2.*;
import static org.basex.util.Token.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.basex.build.BuildException;
import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.io.DataAccess;
import org.basex.io.IO;
import org.basex.io.PrintOutput;
import org.basex.util.ObjectMap;
import org.basex.util.TokenMap;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;

/**
 * This class parses MAB2 data and creates a hierarchical representation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class MAB2Parser extends Parser {
  /** Encoding of MAB2 input. */
  private static final String ENCODING = "iso-8859-1";
  /** Temporary token builder. */
  private final TokenBuilder buffer = new TokenBuilder();
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
  /** Flat database creation. */
  private final boolean flat;
  /** Database builder. */
  private Builder builder;
  /** MAB2 input. */
  private DataAccess input;

  /** Temporary build data. */
  private final byte[][] sig = new byte[500][];
  /** Temporary build data. */
  private final byte[][] pers = new byte[50][];
  /** Temporary build data. */
  private final byte[][] inst = new byte[50][];
  /** Temporary build data. */
  private byte[] mvID;
  /** Temporary build data. */
  private byte[] bibID;
  /** Temporary build data. */
  private byte[] title;
  /** Temporary build data. */
  private byte[] description;
  /** Temporary build data. */
  private byte[] type;
  /** Temporary build data. */
  private byte[] language;
  /** Temporary build data. */
  private byte[] original;
  /** Temporary build data. */
  private byte[] subtitle;
  /** Temporary build data. */
  private byte[] town;
  /** Temporary build data. */
  private byte[] publisher;
  /** Temporary build data. */
  private byte[] year;
  /** Temporary build data. */
  private byte[] format;
  /** Temporary build data. */
  private byte[] details;
  /** Temporary build data. */
  private byte[] note;
  /** Temporary build data. */
  private byte[] isbn;
  /** Temporary build data. */
  private byte[] subject;
  /** Temporary build data. */
  private int nrSigs;
  /** Temporary build data. */
  private int nrPers;
  /** Temporary build data. */
  private int nrInst;
  /** Temporary build data. */
  private boolean shortTitle;
  /** Temporary read position. */
  private long off;
  /** Maximum mediovis id. */
  private int maxid;

  /**
   * Constructor.
   * @param fn filename of the XML document
   * @param pr database properties
   */
  public MAB2Parser(final IO fn, final Prop pr) {
    super(fn, pr);
    flat = prop.is(Prop.MAB2FLAT);
  }

  @Override
  public void parse(final Builder b) throws IOException {
    // define input encoding
    b.encoding(ENCODING);

    // read in indexes
    final String dir = file.getDir();
    index(mediatypes, dir + "/mediatypes.dat");
    index(subjects, dir + "/subjects.dat");
    index(languages, dir + "/lang.dat");
    index(mvids, dir + "/mvids.dat");
    index(lendings, dir + "/lendings.dat");
    index(status, dir + "/status.dat");
    index(posters, dir + "/posters.dat");
    index(genres, dir + "/genres.dat");

    // find maximum mediovis id
    for(int i = 1; i <= mvids.size(); i++) {
      final int id = toInt(mvids.value(i));
      if(maxid < id) maxid = id;
    }

    // create input reference
    input = new DataAccess(new File(file.path()));

    // check beginning of input file
    if(input.read1() != '#' || input.read1() != '#' ||
        input.read1() != '#') {
      throw new BuildException("Invalid MAB2 input (doesn't start with ###)");
    }

    builder = b;
    builder.startDoc(token(file.name()));
    builder.startElem(LIBRARY, atts.reset());

    // find file offsets of all titles
    final Performance p = new Performance();

    /** MAB2 Entries. */
    final ObjectMap<MAB2Entry> ids = new ObjectMap<MAB2Entry>();

    int i = 0;
    byte[] id;
    while((id = id(input)) != null) {
      final long pos = off;
      final byte[] par = par(input);

      final boolean child = par != null;
      final byte[] key = child ? par : id;
      MAB2Entry entry = ids.get(key);
      if(entry == null) {
        entry = new MAB2Entry();
        ids.put(key, entry);
      }
      if(child) entry.add(pos);
      else entry.pos(pos);

      if(Prop.debug) {
        if(++i % 50000 == 0) Main.err(" " + i + "\n");
        else if(i % 5000 == 0) Main.err("!");
        else if(i % 1000 == 0) Main.err(".");
      }
    }

    if(Prop.debug) Main.err("\nParse Offsets (%): %/%\n", ids.size(), p,
        Performance.getMem());

    // create all titles
    for(i = 1; i <= ids.size(); i++) {
      final MAB2Entry entry = ids.value(i);
      final long pos = entry.pos;
      // check if top entry exists...
      final byte[] l = pos != 0 ? addEntry(input, pos, entry.size, null) : null;
      // loop through all children...
      for(int j = 0; j < entry.size; j++) {
        addEntry(input, entry.children[j], 0, l);
      }
      if(entry.size != 0 && pos != 0 && !flat) builder.endElem(MEDIUM);
    }

    if(Prop.debug) Main.err("\nCreate Titles: %/%\n", p, Performance.getMem());

    builder.endElem(LIBRARY);
    builder.endDoc();
    input.close();

    // write the mediovis ids back to disk
    final PrintOutput out = new PrintOutput(dir + "/mvids.dat");
    for(i = 1; i <= mvids.size(); i++) {
      out.print(mvids.key(i));
      out.print('\t');
      out.println(mvids.value(i));
    }
    out.close();
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
        off = in.pos() - 3;
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
  private byte[] par(final DataAccess in) {
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
  private byte[] ident(final DataAccess in) {
    in.read1();
    int l = 0;
    byte b;
    while((b = in.read1()) >= ' ') CACHE[l++] = b;
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
      if(c == delim) return buffer.finish();
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

    mvID = null;
    bibID = null;
    title = null;
    description = null;
    type = null;
    language = null;
    original = null;
    subtitle = null;
    town = null;
    publisher = null;
    year = null;
    format = null;
    details = null;
    note = null;
    isbn = null;
    subject = null;
    nrSigs = 0;
    nrPers = 0;
    nrInst = 0;
    shortTitle = false;

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
          pers[nrPers++] = string(line);
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
        for(int s = 0; s < nrPers; s++) add(PERSON, pers[s]);
        for(int s = 0; s < nrInst; s++) add(INSTITUTE, inst[s]);
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
        for(int s = 0; s < nrSigs; s++) add(SIGNATURE, sig[s]);
        // actually: several subjects/lending numbers per medium..
        for(int s = 0; s < nrSigs; s++) {
          if(subject == null) subject = subjects.get(subject(sig[s]));
        }
        add(SUBJECT, subject);
        add(ISBN, isbn);
        add(POSTER, posters.get(bibID));
        add(GENRE, genres.get(mvID));
        add(STATUS, status.get(bibID));
        add(LENDINGS, lendings.get(bibID));
        if(sb == 0 || flat) builder.endElem(MEDIUM);
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
    builder.text(new TokenBuilder(utf8(cont, ENCODING)));
    builder.endElem(tag);
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
    for(int i = 4; i < l; i++) {
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
    for(int i = 0; i < t.length; i++) if(t[i] == '?' || t[i] == '$') t[i] = '+';
    final TokenBuilder tb = new TokenBuilder();
    for(final byte[] lang : split(t, '+')) {
      final byte[] l = languages.get(lang);
      if(tb.size() != 0) tb.add('+');
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
    for(int s = 4; s < l; s++) {
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
      final DataAccess in = new DataAccess(new File(fn));
      while(true) {
        final byte[] key = find(in, (byte) '\t');
        final byte[] val = find(in, (byte) '\n');
        if(key == null) break;
        hash.add(key, val);
      }
    } catch(final IOException ex) {
      Main.debug(new File(fn).getAbsolutePath() + " not found.");
    }
  }

  /**
   * This is a simple data structure for storing MAB2 entries.
   *
   * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
