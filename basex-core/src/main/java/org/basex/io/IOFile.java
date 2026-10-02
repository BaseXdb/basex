package org.basex.io;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.*;

import javax.xml.transform.stream.*;

import org.basex.core.jobs.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.xml.sax.*;

/**
 * {@link IO} reference, representing a local file or directory path.
 *
 * @author BaseX Team, BSD License
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
   * @param path path reference
   */
  public IOFile(final Path path) {
    this(path.toFile(), "");
  }

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
  public IOFile(final File file, final String last) {
    super(normalize(file.getAbsolutePath(), Strings.endsWith(last, '/') ||
        Strings.endsWith(last, '\\')), false);
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
   * Converts the specified URI to a file path.
   * @param uri URI to be converted
   * @return file path
   */
  static String toPath(final String uri) {
    try {
      return new URI(uri).getPath();
    } catch(final Exception ex) {
      Util.debug(ex);
      // workaround for URIs with invalid characters
      return uri.replaceAll("^" + FILEPREF + '*', "/").replaceFirst("^/([A-Za-z]:)", "$1");
    }
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
  public boolean isExternal() {
    return true;
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
   * Returns the parent of this file or directory.
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
    return io.toArray(IOFile[]::new);
  }

  /**
   * Returns the children of the path that match the specified filter.
   * @param filter file filter (can be {@code null})
   * @return children
   */
  public IOFile[] children(final FileFilter filter) {
    final File[] children = filter == null ? file.listFiles() : file.listFiles(filter);
    if(children == null) return new IOFile[0];

    final ArrayList<IOFile> io = new ArrayList<>(children.length);
    for(final File child : children) {
      io.add(child.isDirectory() ? new IOFile(child + "/") : new IOFile(child));
    }
    return io.toArray(IOFile[]::new);
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
    final Path root = file.toPath();
    walk(filter, (path, attrs) ->
      files.add(root.relativize(path).toString().replace(File.separatorChar, '/')));
    return files;
  }

  /**
   * Returns the summed size of all regular descendant files.
   * @param job job for interrupting the operation (can be {@code null})
   * @return size
   */
  public long size(final Job job) {
    final long[] size = { 0 };
    walk(null, (path, attrs) -> {
      if(job != null) job.checkStop();
      if(attrs.isRegularFile()) size[0] += attrs.size();
    });
    return size[0];
  }

  /**
   * Visits all children, using the attributes of the directory listing.
   * @param visitor visitor for the name and the attributes of a file or directory
   */
  public void children(final BiConsumer<String, BasicFileAttributes> visitor) {
    walk(1, null, (path, attrs) -> visitor.accept(path.getFileName().toString(), attrs));
  }

  /**
   * Visits all non-filtered descendant files, using the attributes of the directory listing.
   * @param filter file filter (can be {@code null})
   * @param visitor visitor for the path and the attributes of a file
   */
  public void walk(final FileFilter filter, final BiConsumer<Path, BasicFileAttributes> visitor) {
    walk(Integer.MAX_VALUE, filter, visitor);
  }

  /**
   * Visits all non-filtered descendant files, and the directories at the maximum depth.
   * @param depth maximum depth
   * @param filter file filter (can be {@code null})
   * @param visitor visitor for the path and the attributes of a file or directory
   */
  private void walk(final int depth, final FileFilter filter,
      final BiConsumer<Path, BasicFileAttributes> visitor) {
    if(!isDir()) return;
    final Path root = file.toPath();
    try {
      Files.walkFileTree(root, EnumSet.of(FileVisitOption.FOLLOW_LINKS), depth,
          new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
          return dir.equals(root) || filter == null || filter.accept(dir.toFile()) ?
            FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
        }
        @Override
        public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) {
          if(filter == null || filter.accept(path.toFile())) visitor.accept(path, attrs);
          return FileVisitResult.CONTINUE;
        }
        @Override
        public FileVisitResult visitFileFailed(final Path path, final IOException ex) {
          Util.debug(ex);
          return FileVisitResult.CONTINUE;
        }
      });
    } catch(final IOException ex) {
      Util.debug(ex);
    }
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
   * Writes the specified input. The input stream is not closed; the caller is responsible.
   * @param is input stream
   * @throws IOException I/O exception
   */
  public void write(final InputStream is) throws IOException {
    Files.copy(is, toPath(), StandardCopyOption.REPLACE_EXISTING);
  }

  /**
   * Deletes the file, or the directory and its descendants.
   * @return {@code true} if the file does not exist or has been deleted
   */
  public boolean delete() {
    try {
      delete(toPath(), null);
      return true;
    } catch(final IOException ex) {
      Util.debug(ex);
      return false;
    }
  }

  /**
   * Deletes a path recursively without following symbolic links.
   * @param path path to be deleted
   * @param job job for interrupting the operation (can be {@code null})
   * @throws IOException I/O exception
   */
  public static void delete(final Path path, final Job job) throws IOException {
    Files.walkFileTree(path, new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
          throws IOException {
        // Windows junctions (reparse points): delete the link, not the target
        if(!attrs.isOther()) return FileVisitResult.CONTINUE;
        delete(dir);
        return FileVisitResult.SKIP_SUBTREE;
      }
      @Override
      public FileVisitResult visitFile(final Path child, final BasicFileAttributes attrs)
          throws IOException {
        if(job != null) job.checkStop();
        delete(child);
        return FileVisitResult.CONTINUE;
      }
      @Override
      public FileVisitResult visitFileFailed(final Path child, final IOException ex)
          throws IOException {
        if(ex instanceof NoSuchFileException) return FileVisitResult.CONTINUE;
        throw ex;
      }
      @Override
      public FileVisitResult postVisitDirectory(final Path dir, final IOException ex)
          throws IOException {
        if(ex != null) throw ex;
        delete(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }

  /**
   * Deletes a single path and removes a DOS read-only attribute that prevents the deletion.
   * @param path path to be deleted
   * @throws IOException I/O exception
   */
  public static void delete(final Path path) throws IOException {
    try {
      Files.delete(path);
    } catch(final AccessDeniedException ex) {
      final DosFileAttributeView view = Files.getFileAttributeView(path,
          DosFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
      if(view == null || !view.readAttributes().isReadOnly()) throw ex;
      view.setReadOnly(false);
      Files.delete(path);
    }
  }

  /**
   * Deletes this directory and its ancestors as long as they are empty.
   * @param root root directory that will be preserved
   */
  public void deleteEmpty(final IOFile root) {
    // File.delete removes a directory only if it is empty
    IOFile dir = this;
    while(dir != null && !dir.file.equals(root.file) && dir.file.delete()) dir = dir.parent();
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
    // copy via stream: a path-based copy locks out readers (database files are opened in rw mode)
    try(InputStream in = Files.newInputStream(toPath())) {
      Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
  }

  /**
   * Moves a file to another target.
   * @param target target
   * @throws IOException I/O exception
   */
  public void moveTo(final IOFile target) throws IOException {
    target.parent().md();
    Files.move(toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
  }

  @Override
  public boolean eq(final IO io) {
    return io instanceof IOFile && equals(pth, io.pth);
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof final IOFile iofile && pth.equals(iofile.pth);
  }

  @Override
  public int hashCode() {
    return pth.hashCode();
  }

  @Override
  public String url() {
    final String path = Strings.startsWith(pth, '/') ? pth.substring(1) : pth;
    final StringBuilder sb = new StringBuilder(FILEPREF).append("//");
    final int pl = path.length();
    for(int p = 0; p < pl; p++) {
      // replace spaces with %20
      final char ch = path.charAt(p);
      if(ch == ' ') sb.append("%20");
      else sb.append(ch);
    }
    return sb.toString();
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
      throw new IOException(ex);
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
      if(!sb.isEmpty()) sb.append('|');
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
   * Returns a normalized file path.
   * @param path input path
   * @param directory directory flag
   * @return path
   */
  private static String normalize(final String path, final boolean directory) {
    final StringList sl = new StringList();
    final int l = path.length();
    final StringBuilder sb = new StringBuilder(l);
    for(int i = 0; i < l; i++) {
      final char ch = path.charAt(i);
      if(ch == '\\' || ch == '/') add(sb, sl);
      else sb.append(ch);
    }
    add(sb, sl);
    if(path.startsWith("\\\\") || path.startsWith("//")) sb.append("//");
    final int size = sl.size();
    for(int s = 0; s < size; s++) {
      if(s != 0 || Strings.startsWith(path, '/')) sb.append('/');
      sb.append(sl.get(s));
    }

    // add slash if original file ends with a slash, or if path is a Windows root directory
    boolean dir = directory;
    if(!dir && Prop.WIN && sb.length() == 2) {
      final char c = Character.toLowerCase(sb.charAt(0));
      dir = c >= 'a' && c <= 'z' && sb.charAt(1) == ':';
    }
    if(dir) sb.append('/');

    return sb.toString();
  }

  /**
   * Adds a directory/file to the path list.
   * @param sb entry to be added
   * @param sl string list
   */
  private static void add(final StringBuilder sb, final StringList sl) {
    String s = sb.toString();
    // switch first Windows letter to upper case
    if(s.length() > 1 && s.charAt(1) == ':' && sl.isEmpty()) {
      s = Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
    if("..".equals(s) && !sl.isEmpty()) {
      // parent step
      if(sl.peek().indexOf(':') == -1) sl.pop();
    } else if(!".".equals(s) && !s.isEmpty()) {
      // skip self and empty steps
      sl.add(s);
    }
    sb.setLength(0);
  }
}
