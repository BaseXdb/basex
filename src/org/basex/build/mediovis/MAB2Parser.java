package org.basex.build.mediovis;

import static org.basex.build.mediovis.MAB2.*;
import static org.basex.util.Token.*;
import java.io.File;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.build.BuildException;
import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.io.PrintOutput;
import org.basex.io.RandomAccess;
import org.basex.util.Array;
import org.basex.util.Map;
import org.basex.util.TokenMap;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This class parses MAB2 data and creates a hierarchical representation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class MAB2Parser extends Parser {
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
  /** Image assignments. */
  private final TokenMap posters = new TokenMap();
  /** Genre assignments. */
  private final TokenMap genres = new TokenMap();
  /** Builder listener. */
  private Builder builder;
  /** MAB2 input. */
  private RandomAccess input;

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
  private int maxid = 0;

  /**
   * Constructor.
   * @param fn filename of the XML document
   */
  public MAB2Parser(final IO fn) {
    super(fn);
  }

  @Override
  public void parse(final Builder b) throws IOException {
    // read in indexes
    final String dir = io.getDir();
    index(mediatypes, dir + "/mediatypes.dat");
    index(subjects, dir + "/subjects.dat");
    index(languages, dir + "/lang.dat");
    index(mvids, dir + "/mvids.dat");
    index(posters, dir + "/posters.dat");
    index(genres, dir + "/genres.dat");

    // find maximum mediovis id
    for(int i = 1; i < mvids.size; i++) {
      final int id = Token.toInt(mvids.value(i));
      if(maxid < id) maxid = id;
    }

    // create input reference
    input = new RandomAccess(io.path());

    // check beginning of input file
    if(input.read() != '#' || input.read() != '#' || input.read() != '#') {
      throw new BuildException("Invalid MAB2 input (doesn't start with ###)");
    }

    b.encoding("ISO-8859-1");
    //b.encoding(Prop.ENCODING);

    builder = b;
    builder.startDoc(token(io.name()));
    builder.startElem(LIBRARY, null);

    // find file offsets of all titles
    final Performance p = new Performance();

    /** MAB2 Entries. */
    final Map<MAB2Entry> ids = new Map<MAB2Entry>();

    int i = 0;
    byte[] id;
    while((id = id(input)) != null) {
      final long pos = off;
      final byte[] par = par(input);

      final boolean child = par != null;
      final byte[] key = child ? par : id;
      MAB2Entry entry = ids.get(key);
      if(entry == null) {
        entry = new MAB2Entry(key);
        ids.add(key, entry);
      }
      if(child) entry.add(pos);
      else entry.pos(pos);

      if(Prop.debug) {
        if(++i % 50000 == 0) BaseX.err(" " + i + "\n");
        else if(i % 5000 == 0) BaseX.err("!");
        else if(i % 1000 == 0) BaseX.err(".");
      }
    }

    if(Prop.debug) {
      BaseX.err("\nParse Offsets (%): %/%\n", ids.size, p.getTimer(),
          Performance.getMem());
    }

    // create all titles
    final int is = ids.size;
    for(i = 1; i < is; i++) {
      final MAB2Entry entry = ids.value(i);
      final long pos = entry.pos;
      // check if top entry exists...
      final byte[] l = pos != 0 ? addEntry(input, pos, entry.size, null) : null;
      // loop through all children...
      for(int j = 0; j < entry.size; j++) {
        addEntry(input, entry.children[j], 0, l);
      }
      if(entry.size != 0 && pos != 0 && !Prop.mab2flat) builder.endElem(MEDIUM);
      
      if(Prop.debug) {
        if(i % 50000 == 0) BaseX.err(" " + i + "\n");
        else if(i % 5000 == 0) BaseX.err("!");
        else if(i % 1000 == 0) BaseX.err(".");
      }
    }

    if(Prop.debug) {
      BaseX.err("\nCreate Titles: %/%\n", p.getTimer(), Performance.getMem());
    }

    builder.endElem(LIBRARY);
    builder.endDoc();
    input.close();

    // write the mediovis ids back to disk
    final PrintOutput out = new PrintOutput(dir + "/mvids.dat");
    for(i = 1; i < mvids.size; i++) {
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
   * @throws IOException I/O exception
   */
  private byte[] id(final RandomAccess in) throws IOException {
    while(in.more()) {
      if(in.read() != '\n') continue;
      final int n = in.read();
      if(n == '0' && in.read() ==  '0' && in.read() == '1') {
        off = in.pos() - 3;
        return text(in);
      }
    }
    return null;
  }

  /**
   * Returns the next parent id.
   * @param in input stream
   * @return id
   * @throws IOException I/O exception
   */
  private byte[] par(final RandomAccess in) throws IOException {
    while(in.more()) {
      if(in.read() != '\n') continue;
      final int b1 = in.read();
      if(b1 == '#' || b1 == '\n') return null;
      if(b1 == '0' && in.read() == '1' && in.read() == '0' ||
         b1 == '4' && in.read() == '5' && in.read() == '3') return text(in);
    }
    return null;
  }

  /**
   * Returns the next text.
   * @param in input stream
   * @return next text
   * @throws IOException I/O exception
   */
  private byte[] text(final RandomAccess in) throws IOException {
    in.read();
    int l = 0;
    int b;
    while((b = in.read()) >= ' ') CACHE[l++] = (byte) b;
    return Array.finish(CACHE, l);
  }

  /** Buffer. */
  private static final byte[] CACHE = new byte[16];

  /**
   * Get all characters up to specified character.
   * @param in input stream
   * @param delim delimiter
   * @return byte array
   * @throws IOException I/O exception
   */
  private byte[] find(final RandomAccess in, final byte delim)
      throws IOException {

    buffer.reset();
    while(in.more()) {
      final int c = in.read();
      if(c == delim) return buffer.finish();
      if(c < ' ') continue;
      buffer.add((byte) c);
    }
    return null;
  }

  /**
   * Adds an entry.
   * @param in input stream
   * @param pos file offset to start from
   * @param sub number of subordinate titles
   * @param last last title
   * @return last title
   * @throws IOException I/O exception
   */
  private byte[] addEntry(final RandomAccess in, final long pos, final int sub,
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

        final int n = Token.toInt(line, 0, 3);
        if(n == 1) {
          if(bibID == null) {
            bibID = string(line);
            mvID = mvids.get(bibID);
            if(mvID == null) {
              mvID = Token.token(++maxid);
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
        if(sub != 0 && !Prop.mab2flat) atts.add(MAX, Token.token(sub));

        // merge super and sub titles
        if(last != null) {
          if(title == null) title = last;
          else if(!eq(last, title)) title = concat(last, SEMI, title);
        }

        // add line below to omit root nodes
        builder.startElem(MEDIUM, atts);
        addTag(TYPE, type);
        addTag(LANGUAGE, language);
        for(int s = 0; s < nrPers; s++) addTag(PERSON, pers[s]);
        for(int s = 0; s < nrInst; s++) addTag(INSTITUTE, inst[s]);
        addTag(ORIGINAL, original);
        addTag(TITLE, title);
        addTag(SUBTITLE, subtitle);
        addTag(DESCRIPTION, description);
        addTag(TOWN, town);
        addTag(PUBLISHER, publisher);
        addTag(YEAR, year);
        addTag(FORMAT, format);
        addTag(DETAILS, details);
        addTag(NOTE, note);
        for(int s = 0; s < nrSigs; s++) addTag(SIGNATURE, sig[s]);
        // actually: several subjects/lending numbers per medium..
        for(int s = 0; s < nrSigs; s++) {
          if(subject == null) subject = subjects.get(subject(sig[s]));
        }
        addTag(SUBJECT, subject);
        addTag(ISBN, isbn);
        addTag(POSTER, posters.get(bibID));
        addTag(GENRE, genres.get(mvID));
        if(sub == 0 || Prop.mab2flat) builder.endElem(MEDIUM);

        return title;
      }
    }
  }

  /**
   * Adds a tag and a content node.
   * @param tag tag to be added
   * @param cont content to be added
   * @throws IOException in case of parse or write problems
   */
  private void addTag(final byte[] tag, final byte[] cont) throws IOException {
    if(cont != null) builder.nodeAndText(tag, cont);
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
    return c != 0 ? Array.finish(n, c) : null;
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

    int oy = yr != null ? Token.toInt(yr) : 0;
    if(oy >= 1400 && oy <= 1950) return yr;

    final byte[] y = Array.create(line, j + 1, 4);
    oy = Token.toInt(y);
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
    for(byte[] lang : split(t, '+')) {
      final byte[] l = languages.get(lang);
      if(tb.size != 0) tb.add('+');
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
      else if(b == -84)  b = ' ';
      else if(b == '<')  b = '[';
      else if(b == '>')  b = ']';
      if(b == ' ' && (space || s == 4)) continue;
      space = b == ' ';
      tmp[c++] = b;
    }

    if(c == tmp.length) return tmp;
    return Array.finish(tmp, c);
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
    return Array.create(line, s, line.length - s);
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

  @Override
  public String head() {
    return "Convert MAB2 data...";
  }

  @Override
  public String det() {
    return "Converting...";
  }

  @Override
  public double percent() {
    return 0;
  }

  /**
   * Fills the specified hash with the file input.
   * @param hash hash to be filled.
   * @param fn file to be read
   */
  private void index(final TokenMap hash, final String fn) {
    try {
      final RandomAccess in = new RandomAccess(fn);
      while(true) {
        final byte[] key = find(in, (byte) '\t');
        final byte[] val = find(in, (byte) '\n');
        if(key == null) break;
        hash.add(key, val);
      }
    } catch(final IOException e) {
      BaseX.debug(new File(fn).getAbsolutePath() + " not found.");
    }
  }
}
