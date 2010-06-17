package org.basex.gui;

import java.awt.Container;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import org.basex.gui.layout.BaseXLayout;
import org.basex.util.Token;

/**
 * This class organizes icons and data types for common file types.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class GUIFS {
  /** File Types. */
  public enum Type {
    /** Unknown Content. */ NONE,
    /** Textual Content. */ TEXT,
    /** Image Content.   */ IMAGE;
  }

  /**
   * Recognized file types.
   * All file types are organized in sub arrays. The first entry in
   * an sub array refers to the internal file type. The second one is the
   * file suffix which is equal to the .png file name on disk. If the file
   * name is different, the third string points to that name.
   */
  private static final Object[][] TYPES = {
    { Type.NONE, "unknown type", "unknown" },
    { Type.NONE, "mpeg4 video", "3gp", "mpg" },
    { Type.NONE, "7zip archive", "7z", "zip" },
    { Type.NONE, "ace archive", "ace", "zip" },
    { Type.TEXT, "type1 font", "afm", "pfm" },
    { Type.TEXT, "perl script", "al", "pl" },
    { Type.NONE, "animated cursor", "ani", "cur" },
    { Type.NONE, "ape audio", "ape", "audio" },
    { Type.NONE, "arj archive", "arj", "zip" },
    { Type.NONE, "au audio", "au", "audio" },
    { Type.NONE, "avi video", "avi" },
    { Type.NONE, "library", "ax", "dll" },
    { Type.TEXT, "batch script", "bat" },
    { Type.NONE, "binary file", "bin" },
    { Type.NONE, "bitmap image", "bmp", "pic" },
    { Type.NONE, "bz2 archive", "bz2", "zip" },
    { Type.TEXT, "c code", "c" },
    { Type.NONE, "c64 archive", "c64" },
    { Type.NONE, "cab archive", "cab", "zip" },
    { Type.NONE, "security catalog", "cat" },
    { Type.TEXT, "configuration file", "cfg", "ini" },
    { Type.TEXT, "configuration file", "cong", "ini" },
    { Type.NONE, "compressed help document", "chm" },
    { Type.NONE, "java class file", "class" },
    { Type.NONE, "ms-dos runnable", "com" },
    { Type.NONE, "system controls", "cpl", "dll" },
    { Type.TEXT, "c++ code", "cpp" },
    { Type.TEXT, "html style sheet", "css", "htm" },
    { Type.NONE, "iso cuesheet", "cue", "iso" },
    { Type.NONE, "cursor", "cur" },
    { Type.NONE, "c64 disk", "d64", "c64" },
    { Type.NONE, "data file", "dat", "dll" },
    { Type.TEXT, "diz info file", "diz", "txt" },
    { Type.NONE, "windows library", "dll" },
    { Type.NONE, "word document", "doc" },
    { Type.NONE, "word template", "dot", "doc" },
    { Type.NONE, "windows driver", "drv", "dll" },
    { Type.TEXT, "xml document type description", "dtd", "xml" },
    { Type.NONE, "postscript dvi", "dvi" },
    { Type.NONE, "windows runnable", "exe" },
    { Type.NONE, "bitmap font", "fon" },
    { Type.NONE, "windows help file", "gid", "hlp" },
    { Type.IMAGE, "gif image", "gif" },
    { Type.TEXT, "gnuplot script", "gpl", "txt" },
    { Type.NONE, "gzip archive", "gz", "zip" },
    { Type.NONE, "gzip Archive", "gzip", "zip" },
    { Type.TEXT, "c header code", "h" },
    { Type.NONE, "windows help file", "hlp" },
    { Type.TEXT, "html document", "htm" },
    { Type.TEXT, "html document", "html", "htm" },
    { Type.NONE, "icon library", "icl", "ico" },
    { Type.NONE, "icon image", "ico" },
    { Type.TEXT, "driver information", "inf", "ini" },
    { Type.TEXT, "initialization file", "ini" },
    { Type.NONE, "iso archive", "iso" },
    { Type.TEXT, "midp descriptor", "jad" },
    { Type.NONE, "java archive", "jar" },
    { Type.TEXT, "java code", "java" },
    { Type.IMAGE, "jpeg image", "jpg" },
    { Type.IMAGE, "jpeg image", "jpeg", "jpg" },
    { Type.TEXT, "javascript", "js" },
    { Type.TEXT, "latex document", "latex", "tex" },
    { Type.NONE, "lha archive", "lha", "zip" },
    { Type.TEXT, "language file", "lng", "txt" },
    { Type.NONE, "windows link", "lnk" },
    { Type.TEXT, "lexical data", "lex" },
    { Type.TEXT, "mp3 playlist", "m3u", "mp3" },
    { Type.NONE, "mpeg4 audio", "m4a", "audio" },
    { Type.TEXT, "mailbox document", "mbs" },
    { Type.TEXT, "java manifest", "mf", "java" },
    { Type.NONE, "midi audio", "mid", "audio" },
    { Type.NONE, "mod audio", "mod", "audio" },
    { Type.NONE, "quicktime video", "mov" },
    { Type.NONE, "mp3 audio", "mp3" },
    { Type.NONE, "mpc audio", "mpc", "audio" },
    { Type.NONE, "mpeg video", "mpeg", "mpg" },
    { Type.NONE, "mpeg video", "mpg" },
    { Type.NONE, "windows installer", "msi" },
    { Type.NONE, "windows installer", "msp", "msi" },
    { Type.NONE, "open math", "odf" },
    { Type.NONE, "open draw", "odg" },
    { Type.NONE, "open presentation", "odp" },
    { Type.NONE, "open spreadsheet", "ods" },
    { Type.NONE, "open text", "odt" },
    { Type.NONE, "ogg audio", "ogg", "audio" },
    { Type.NONE, "ogg video", "ogm", "mpg" },
    { Type.NONE, "opentype font", "otf" },
    { Type.NONE, "open text template", "ott", "odt" },
    { Type.NONE, "pdf document", "pdf" },
    { Type.NONE, "type1 font", "pfb", "pfm" },
    { Type.NONE, "type1 font", "pfm" },
    { Type.TEXT, "php script", "php" },
    { Type.TEXT, "php script", "php3", "php" },
    { Type.NONE, "windows link", "pif", "lnk" },
    { Type.TEXT, "perl script", "pl" },
    { Type.TEXT, "perl module", "pm", "pl" },
    { Type.NONE, "library", "pnf", "dll" },
    { Type.IMAGE, "png image", "png" },
    { Type.TEXT, "perl document", "pod", "pl" },
    { Type.NONE, "powerpoint slideshow", "pps" },
    { Type.NONE, "powerpoint document", "ppt" },
    { Type.NONE, "c64 program", "prg", "c64" },
    { Type.TEXT, "postscript document", "ps" },
    { Type.NONE, "paintshop image", "psp" },
    { Type.NONE, "paintshop image", "pspimage", "psp" },
    { Type.TEXT, "paintshop script", "pspscript", "psp" },
    { Type.TEXT, "python script", "py" },
    { Type.NONE, "compiled python script", "pyc" },
    { Type.NONE, "python object", "pyo", "pyc" },
    { Type.NONE, "real audio", "ra", "rm" },
    { Type.NONE, "rar archive", "rar" },
    { Type.TEXT, "c resource file", "rc" },
    { Type.TEXT, "windows registry file", "reg" },
    { Type.NONE, "real video", "rm" },
    { Type.NONE, "richtext document", "rtf", "doc" },
    { Type.TEXT, "html document", "shtm", "htm" },
    { Type.TEXT, "html document", "shtml", "htm" },
    { Type.NONE, "sid audio", "sid", "audio" },
    { Type.TEXT, "movie subtitle", "smi", "sub" },
    { Type.NONE, "linux library", "so", "dll" },
    { Type.TEXT, "movie subtitle", "srt", "sub" },
    { Type.TEXT, "movie subtitle", "ssa", "sub" },
    { Type.TEXT, "latex style", "sty", "tex" },
    { Type.TEXT, "movie subtitle", "sub" },
    { Type.NONE, "subversion file", "svn-base", "svn" },
    { Type.NONE, "subversion file", "svn-work", "svn" },
    { Type.NONE, "system file", "sys" },
    { Type.NONE, "c64 tape image", "t64", "c64" },
    { Type.NONE, "tarball archive", "tar", "zip" },
    { Type.TEXT, "latex document", "tex" },
    { Type.NONE, "type1 font", "tfm", "pfm" },
    { Type.NONE, "tga image", "tga", "pic" },
    { Type.NONE, "tgz archive", "tgz", "zip" },
    { Type.NONE, "tif image", "tif", "pic" },
    { Type.NONE, "tif image", "tiff", "pic" },
    { Type.TEXT, "latex document", "tpm", "tex" },
    { Type.NONE, "truetype font", "ttf" },
    { Type.TEXT, "text document", "txt" },
    { Type.TEXT, "url reference", "url", "htm" },
    { Type.TEXT, "visualbasic script", "vbs" },
    { Type.NONE, "type1 font", "vf", "pfm" },
    { Type.NONE, "wave audio", "wav", "audio" },
    { Type.NONE, "windows media file", "wmf", "pic" },
    { Type.NONE, "windows movie", "wmv", "wmv" },
    { Type.NONE, "excel spreadsheet", "xls" },
    { Type.NONE, "xm audio", "xm", "audio" },
    { Type.TEXT, "xml document", "xml" },
    { Type.TEXT, "xquery script", "xq", "xml" },
    { Type.TEXT, "xslt script", "xsl" },
    { Type.NONE, "z64 archive", "z64", "c64" },
    { Type.NONE, "zip archive", "zip" },
  };

  /** Singleton instance. */
  private static final GUIFS INSTANCE = new GUIFS();

  /** Closed folder. */
  public final Image[] folder1 = new Image[2];
  /** Opened folder. */
  public final Image[] folder2 = new Image[2];

  /** File type icons. */
  private final Image[][] images;
  /** File types. */
  private final Type[] type;
  /** File type description. */
  private final String[] desc;

  /** Size of hash map. */
  private static final int MAPSIZE = 255;
  /** Hash table buckets. */
  private final int[] bucket;
  /** Pointers to the next token. */
  private final int[] next;
  /** Pointers to the next token. */
  private final byte[][] token;
  /** Number of file types. */
  private int size;

  /** Preventing class instantiation. */
  private GUIFS() {
    bucket = new int[MAPSIZE + 1];
    next = new int[MAPSIZE + 1];

    token = new byte[MAPSIZE][];
    images = new Image[2][MAPSIZE];
    type = new Type[MAPSIZE];
    desc = new String[MAPSIZE];
    size = 1;

    final Container cont = new Container();
    final MediaTracker tracker = new MediaTracker(cont);
    final Toolkit tk = cont.getToolkit();

    try {
      int i = 0;
      while(++i < TYPES.length) {
        final int j = put(Token.token(TYPES[i][2].toString()), 0);
        type[j] = (Type) TYPES[i][0];
        desc[j] = TYPES[i][1].toString();
        final String img = TYPES[i][TYPES[i].length - 1].toString();
        images[0][j] = add(img, tracker, tk, i);
        images[1][j] = add(img + '2', tracker, tk, i);
      }
      desc[0] = TYPES[0][1].toString();
      folder1[0] = add("folder1", tracker, tk, i++);
      folder1[1] = add("folder12", tracker, tk, i++);
      folder2[0] = add("folder2", tracker, tk, i++);
      folder2[1] = add("folder22", tracker, tk, i++);
      images[0][0] = add("unknown", tracker, tk, i++);
      images[1][0] = add("unknown2", tracker, tk, i++);
      tracker.waitForAll();
    } catch(final Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Returns the only instance of this class.
   * @return instance
   */
  public static GUIFS get() {
    return INSTANCE;
  }

  /**
   * Returns URL for the specified image.
   * @param name name of image
   * @param tracker media tracker
   * @param tk toolkit
   * @param n tracker id
   * @return image
   */
  private Image add(final String name, final MediaTracker tracker,
      final Toolkit tk, final int n) {
    final Image img = tk.getImage(BaseXLayout.imageURL("file-" + name));
    tracker.addImage(img, n);
    return img;
  }

  /**
   * Finds the specified token and returns its unique id.
   * @param tok token to be found
   * @param t start position
   * @return token id
   */
  private int put(final byte[] tok, final int t) {
    final int h = hash(tok, t) & MAPSIZE;
    next[size] = bucket[h];
    bucket[h] = size;
    token[size] = tok;
    return size++;
  }

  /**
   * Returns the file type for the specified filename.
   * @param name file name
   * @return type
   */
  public short type(final byte[] name) {
    int i = name.length;
    while(--i > 0 && name[i] != '.');
    return (short) (i == 0 ? 0 : get(name, ++i));
  }

  /**
   * Returns the suffix image for the specified file.
   * @param name file name
   * @param n image offset (0/1)
   * @return image
   */
  public Image images(final byte[] name, final int n) {
    return images[n][type(name)];
  }

  /**
   * Returns the suffix image for the specified file.
   * @param name file name
   * @param big small/big image
   * @return image
   */
  public Image images(final byte[] name, final boolean big) {
    return images[big ? 1 : 0][get(name, 0)];
  }

  /**
   * Returns the suffix image for the specified file.
   * @param name file name
   * @return image
   */
  public Type mime(final byte[] name) {
    return type[type(name)];
  }

  /**
   * Finds the specified token and returns its unique id.
   * @param tok token to be found
   * @param s start position
   * @return token id or 0 if token was not found
   */
  private int get(final byte[] tok, final int s) {
    final int p = hash(tok, s) & MAPSIZE;
    for(int tid = bucket[p]; tid != 0; tid = next[tid]) {
      if(equal(token[tid], tok, s)) return tid;
    }
    return 0;
  }

  /**
   * Compares two character arrays for equality.
   * @param t1 indexed token
   * @param t2 token to be compared
   * @param s start position
   * @return true if the arrays are equal
   */
  private boolean equal(final byte[] t1, final byte[] t2, final int s) {
    final int tl = t1.length;
    if(tl != t2.length - s) return false;
    for(int t = 0; t < tl; t++) {
      if(Token.lc(t1[t]) != Token.lc(t2[s + t])) return false;
    }
    return true;
  }

  /**
   * Calculates a hash code for the specified token.
   * @param tok specified token
   * @param s start position
   * @return hash code
   */
  private int hash(final byte[] tok, final int s) {
    int h = 0;
    for(int t = s; t < tok.length; t++) h = (h << 5) - h + Token.lc(tok[t]);
    return h;
  }
}
