package org.basex.io;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

import javax.xml.transform.stream.*;

import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.xml.sax.*;

/**
 * {@link IO} reference, representing a local file or directory path.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class IOFile extends IO {
  /** Ignore files starting with a dot. */
  public static final FileFilter NO_HIDDEN = file -> !Strings.startsWith(file.getName(), '.');
  /** Pattern for valid file names. */
  private static final Pattern VALIDNAME =
      Pattern.compile("^[^\\\\/" + (Prop.WIN ? ":*?\"<>|" : "") + "]+$");

  /** Absolute flag. */
  private final boolean absolute;
  /** File reference. */
  private final File file;

  /**
   * Constructor.
   * @param file file reference
   */
  public IOFile(final File file) {
    this(file, "");
  }

  /**
   * Constructor.
   * @param path file path
   */
  public IOFile(final String path) {
    this(new File(path), path);
  }

  /**
   * Constructor.
   * @param dir parent directory string
   * @param child child directory string
   */
  public IOFile(final String dir, final String child) {
    this(new File(dir, child), child);
  }

  /**
   * Constructor.
   * @param dir directory string
   * @param child child path string
   */
  public IOFile(final IOFile dir, final String child) {
    this(new File(dir.file, child), child);
  }

  /**
   * Constructor.
   * @param file file reference
   * @param last last path segment; if it ends with a slash, it indicates a directory
   */
  private IOFile(final File file, final String last) {
    super(create(file.getAbsolutePath(), Strings.endsWith(last, '/') ||
        Strings.endsWith(last, '\\')));
    boolean abs = file.isAbsolute();
    this.file = abs ? file : new File(pth);
    // Windows: checks if the original file path starts with a slash
    if(!abs && Prop.WIN) {
      final String p = file.getPath();
      abs = Strings.startsWith(p, '/') || Strings.startsWith(p, '\\');
    }
    absolute = abs;
  }

  /**
   * Returns the file reference.
   * @return file reference
   */
  public File file() {
    return file;
  }

  /**
   * Creates a new instance of this file.
   * @return success flag
   */
  public boolean touch() {
    try {
      Files.createFile(toPath());
      return true;
    } catch(final IOException ex) {
      Util.debug(ex);
      return false;
    }
  }

  @Override
  public byte[] read() throws IOException {
    return Files.readAllBytes(toPath());
  }

  @Override
  public boolean exists() {
    return file.exists();
  }

  @Override
  public boolean isDir() {
    return file.isDirectory();
  }

  @Override
  public boolean isAbsolute() {
    return absolute;
  }

  @Override
  public long timeStamp() {
    return file.lastModified();
  }

  @Override
  public long length() {
    return file.length();
  }

  @Override
  public InputSource inputSource() {
    return new InputSource(url());
  }

  @Override
  public StreamSource streamSource() {
    return new StreamSource(pth);
  }

  @Override
  public FileInputStream inputStream() throws IOException {
    return new FileInputStream(file);
  }

  /**
   * Returns an output stream.
   * @return output stream
   * @throws IOException I/O exception
   */
  public FileOutputStream outputStream() throws IOException {
    return new FileOutputStream(file);
  }

  /**
   * Resolves two paths.
   * @param path file path (relative or absolute)
   * @return resulting path
   */
  public IOFile resolve(final String path) {
    final IOFile f = new IOFile(path);
    return f.absolute ? f : new IOFile(isDir() ? pth : dir(), path);
  }

  /**
   * Recursively creates the directory if it does not exist yet.
   * @return {@code true} if the directory exists or has been created
   */
  public boolean md() {
    return file.exists() || file.mkdirs();
  }

  /**
   * Returns the parent of this file or directory or {@code null} if there is no parent directory.
   * @return directory or {@code null}
   */
  public IOFile parent() {
    final String parent = file.getParent();
    return parent == null ? null : new IOFile(parent + '/');
  }

  /**
   * Returns the children of the path.
   * @return children
   */
  public IOFile[] children() {
    return children((FileFilter) null);
  }

  /**
   * Returns the children of the path that match the specified regular expression.
   * @param regex regular expression pattern
   * @return children
   */
  public IOFile[] children(final String regex) {
    final File[] children = file.listFiles();
    if(children == null) return new IOFile[0];

    final ArrayList<IOFile> io = new ArrayList<>();
    final Pattern pattern = Pattern.compile(regex, Prop.CASE ? 0 : Pattern.CASE_INSENSITIVE);
    for(final File child : children) {
      if(pattern.matcher(child.getName()).matches()) {
        io.add(child.isDirectory() ? new IOFile(child.getPath() + '/') : new IOFile(child));
      }
    }
    return io.toArray(new IOFile[0]);
  }

  /**
   * Returns the children of the path that match the specified filter.
   * @param filter file filter
   * @return children
   */
  public IOFile[] children(final FileFilter filter) {
    final File[] children = filter == null ? file.listFiles() : file.listFiles(filter);
    if(children == null) return new IOFile[0];

    final ArrayList<IOFile> io = new ArrayList<>(children.length);
    for(final File child : children) {
      io.add(child.isDirectory() ? new IOFile(child + "/") : new IOFile(child));
    }
    return io.toArray(new IOFile[0]);
  }

  /**
   * Returns the relative paths of all descendant files (excluding directories).
   * @return relative paths
   */
  public StringList descendants() {
    return descendants(null);
  }

  /**
   * Returns the relative paths of all descendant non-filtered files (excluding directories).
   * @param filter file filter
   * @return relative paths
   */
  public StringList descendants(final FileFilter filter) {
    final StringList files = new StringList();
    if(isDir()) {
      final int offset = path().length() + (Strings.endsWith(path(), '/') ? 0 : 1);
      addDescendants(this, files, filter, offset);
    }
    return files;
  }

  /**
   * Writes the specified string as UTF8.
   * @param string string
   * @throws IOException I/O exception
   */
  public void write(final String string) throws IOException {
    write(Token.token(string));
  }

  /**
   * Writes the specified byte array.
   * @param bytes bytes
   * @throws IOException I/O exception
   */
  public void write(final byte[] bytes) throws IOException {
    Files.write(toPath(), bytes);
  }

  /**
   * Writes the specified input. The specified input stream is eventually closed.
   * @param is input stream
   * @throws IOException I/O exception
   */
  public void write(final InputStream is) throws IOException {
    try(BufferInput in = BufferInput.get(is); BufferOutput out = new BufferOutput(this)) {
      for(int i; (i = in.read()) != -1;) out.write(i);
    }
  }

  /**
   * Deletes the file, or the directory and its children.
   * @return {@code true} if the file does not exist or has been deleted
   */
  public boolean delete() {
    boolean ok = true;
    if(file.exists()) {
      if(isDir()) {
        for(final IOFile ch : children()) ok &= ch.delete();
      }
      try {
        Files.delete(toPath());
      } catch(final IOException ex) {
        Util.debug(ex);
        return false;
      }
    }
    return ok;
  }

  /**
   * Renames a file to the specified path. The path must not exist yet.
   * @param target target reference
   * @return success flag
   */
  public boolean rename(final IOFile target) {
    return file.renameTo(target.file);
  }

  /**
   * Copies a file to another target.
   * @param target target
   * @throws IOException I/O exception
   */
  public void copyTo(final IOFile target) throws IOException {
    // create parent directory of target file
    target.parent().md();
    Files.copy(toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
  }

  @Override
  public boolean eq(final IO io) {
    return io instanceof IOFile && (Prop.CASE ? pth.equals(io.pth) : pth.equalsIgnoreCase(io.pth));
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof IOFile && pth.equals(((IOFile) obj).pth);
  }

  @Override
  public String url() {
    final String path = Strings.startsWith(pth, '/') ? pth.substring(1) : pth;
    final TokenBuilder tb = new TokenBuilder().add(FILEPREF).add("//");
    final int pl = path.length();
    for(int p = 0; p < pl; p++) {
      // replace spaces with %20
      final char ch = path.charAt(p);
      if(ch == ' ') tb.add("%20");
      else tb.add(ch);
    }
    return tb.toString();
  }

  /**
   * Opens the file externally.
   * @throws IOException I/O exception
   */
  public void open() throws IOException {
    final String[] args;
    if(Prop.WIN) {
      args = new String[] { "rundll32", "url.dll,FileProtocolHandler", pth };
    } else if(Prop.MAC) {
      args = new String[] { "/usr/bin/open", pth };
    } else {
      args = new String[] { "xdg-open", pth };
    }
    new ProcessBuilder(args).directory(parent().file).start();
  }

  /**
   * Returns a native file path representation. If normalization fails, returns the original path.
   * @return path
   */
  public IOFile normalize() {
    try {
      final Path path = toPath().toRealPath();
      return new IOFile(path + (Files.isDirectory(path) ? "/" : ""));
    } catch(final IOException ex) {
      Util.debug(ex);
      return this;
    }
  }

  /**
   * Checks if a file is hidden.
   * @return result of check
   */
  public boolean isHidden() {
    return file.isHidden() || Strings.startsWith(name(), '.') || name().equals("node_modules");
  }

  /**
   * Checks if the parent directory of this file can be ignored.
   * @return result of check
   */
  public boolean ignore() {
    return name().equals(".ignore");
  }

  // STATIC METHODS ===============================================================================

  /**
   * Returns a {@link Path} instance of this file.
   * @return path
   * @throws IOException I/O exception
   */
  private Path toPath() throws IOException {
    try {
      return Paths.get(pth);
    } catch(final InvalidPathException ex) {
      Util.debug(ex);
      throw new IOException(ex);
    }
  }

  /**
   * Adds the relative paths of all descendant files to the specified list.
   * @param io current file
   * @param files file list
   * @param filter file filter
   * @param offset string length of root path
   */
  private static void addDescendants(final IOFile io, final StringList files,
      final FileFilter filter, final int offset) {
    if(io.isDir()) {
      for(final IOFile child : io.children(filter)) {
        addDescendants(child, files, filter, offset);
      }
    } else {
      if(filter == null || filter.accept(io.file)) {
        files.add(io.path().substring(offset));
      }
    }
  }

  /**
   * Checks if the specified string is a valid file name.
   * @param name file name
   * @return result of check
   */
  public static boolean isValidName(final String name) {
    return VALIDNAME.matcher(name).matches();
  }

  /**
   * Checks if the specified string is a valid file reference.
   * @param path path string
   * @return result of check
   */
  public static boolean isValid(final String path) {
    // check if path starts with Windows drive letter
    final int c = path.indexOf(':');
    return c == -1 || !Prop.WIN || c == 1 && Token.letter(path.charAt(0)) &&
        (path.indexOf('/') == 2 || path.indexOf('\\') == 2);
  }

  /**
   * Converts a name filter (glob) to a regular expression.
   * @param glob filter
   * @return regular expression
   */
  public static String regex(final String glob) {
    return regex(glob, true);
  }

  /**
   * Converts a file filter (glob) to a regular expression. A filter may
   * contain asterisks (*) and question marks (?); commas (,) are used to
   * separate multiple filters.
   * @param glob filter
   * @param substring accept substring in the result
   * @return regular expression
   */
  public static String regex(final String glob, final boolean substring) {
    final StringBuilder sb = new StringBuilder();
    for(final String globs : Strings.split(glob, ',')) {
      final String glb = globs.trim();
      if(sb.length() != 0) sb.append('|');
      // loop through single pattern
      boolean suffix = false;
      final int gl = glb.length();
      for(int g = 0; g < gl; g++) {
        char ch = glb.charAt(g);
        if(ch == '*') {
          // don't allow other dots if pattern ends with a dot
          suffix = true;
          sb.append(Strings.endsWith(glb, '.') ? "[^.]" : ".");
        } else if(ch == '?') {
          ch = '.';
          suffix = true;
        } else if(ch == '.') {
          suffix = true;
          // last character is dot: disallow file suffix
          if(g + 1 == glb.length()) break;
          sb.append('\\');
        } else if(!Character.isLetterOrDigit(ch)) {
          sb.append('\\');
        }
        sb.append(ch);
      }
      if(!suffix && substring) sb.append(".*");
    }
    return Prop.CASE ? sb.toString() : sb.toString().toLowerCase(Locale.ENGLISH);
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Creates a path.
   * @param path input path
   * @param directory directory flag
   * @return path
   */
  private static String create(final String path, final boolean directory) {
    final StringList sl = new StringList();
    final int l = path.length();
    final TokenBuilder tb = new TokenBuilder(l);
    for(int i = 0; i < l; ++i) {
      final char ch = path.charAt(i);
      if(ch == '\\' || ch == '/') add(tb, sl);
      else tb.add(ch);
    }
    add(tb, sl);
    if(path.startsWith("\\\\") || path.startsWith("//")) tb.add("//");
    final int size = sl.size();
    for(int s = 0; s < size; ++s) {
      if(s != 0 || Strings.startsWith(path, '/')) tb.add('/');
      tb.add(sl.get(s));
    }

    // add slash if original file ends with a slash, or if path is a Windows root directory
    boolean dir = directory;
    if(!dir && Prop.WIN && tb.size() == 2) {
      final int c = Character.toLowerCase(tb.get(0));
      dir = c >= 'a' && c <= 'z' && tb.get(1) == ':';
    }
    if(dir) tb.add('/');

    return tb.toString();
  }

  /**
   * Adds a directory/file to the path list.
   * @param tb entry to be added
   * @param sl string list
   */
  private static void add(final TokenBuilder tb, final StringList sl) {
    String s = tb.toString();
    // switch first Windows letter to upper case
    if(s.length() > 1 && s.charAt(1) == ':' && sl.isEmpty()) {
      s = Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
    if("..".equals(s) && !sl.isEmpty()) {
      // parent step
      if(sl.get(sl.size() - 1).indexOf(':') == -1) sl.remove(sl.size() - 1);
    } else if(!".".equals(s) && !s.isEmpty()) {
      // skip self and empty steps
      sl.add(s);
    }
    tb.reset();
  }

}
