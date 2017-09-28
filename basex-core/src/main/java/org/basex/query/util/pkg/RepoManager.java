package org.basex.query.util.pkg;

import static org.basex.core.Text.*;
import static org.basex.query.QueryError.*;
import static org.basex.query.util.pkg.PkgText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Repository manager.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Rositsa Shadura
 */
public final class RepoManager {
  /** Main-class pattern. */
  private static final Pattern MAIN_CLASS = Pattern.compile("^Main-Class: *(.+?) *$");
  /** Ignore files starting with a dot. */
  private static final FileFilter DOT_FILE_FILTER = file -> !file.getName().startsWith(".");
  /** Context. */
  private final Context context;
  /** Input info. */
  private final InputInfo info;

  /**
   * Constructor.
   * @param context database context
   */
  public RepoManager(final Context context) {
    this(context, null);
  }

  /**
   * Constructor.
   * @param context database context
   * @param info input info
   */
  public RepoManager(final Context context, final InputInfo info) {
    this.context = context;
    this.info = info;
  }

  /**
   * Installs a package.
   * @param path package path
   * @return {@code true} if package was replaced
   * @throws QueryException query exception
   */
  public boolean install(final String path) throws QueryException {
    // check if package exists, and cache contents
    final IO io = IO.get(path);
    final byte[] cont;
    try {
      cont = io.read();
    } catch(final IOException ex) {
      Util.debug(ex);
      throw BXRE_WHICH_X.get(info, path);
    }

    try {
      if(io.hasSuffix(IO.XQSUFFIXES)) return installXQ(cont, path);
      if(io.hasSuffix(IO.JARSUFFIX)) return installJAR(cont, path);
      return installXAR(cont);
    } catch(final IOException ex) {
      throw BXRE_PARSE_X_X.get(info, io.name(), ex);
    }
  }

  /**
   * Returns all installed packages in a table.
   * @return table
   */
  public Table table() {
    final Table t = new Table();
    t.description = PACKAGES_X;
    t.header.add(NAME);
    t.header.add(VERSINFO);
    t.header.add(TYPE);
    t.header.add(PATH);
    for(final Pkg pkg : all()) {
      final TokenList tl = new TokenList();
      tl.add(pkg.name());
      tl.add(pkg.version());
      tl.add(pkg.type());
      tl.add(pkg.dir());
      t.contents.add(tl);
    }
    return t;
  }

  /**
   * Returns a list of all package ids.
   * @return packages
   */
  public StringList list() {
    final StringList sl = new StringList();
    for(final Pkg pkg : all()) sl.add(pkg.id());
    return sl;
  }

  /**
   * Removes a package from the package repository.
   * @param name name or id of the package
   * @throws QueryException query exception
   */
  public void delete(final String name) throws QueryException {
    // find registered packages to be deleted
    boolean deleted = false;
    final EXPathRepo repo = context.repo;
    for(final Pkg pkg : all()) {
      final String dir = pkg.dir();
      if(pkg.name().equals(name) || pkg.id().equals(name) || dir.equals(name)) {
        if(pkg.expath()) {
          // check if package to be deleted participates in a dependency
          final String dep = primary(pkg);
          if(dep != null) throw BXRE_DEP_X_X.get(info, dep, name);
          // delete files in main-memory repository
          repo.delete(pkg);
        }
        final IOFile pkgf = repo.path(dir);
        if(!pkgf.delete()) throw BXRE_DELETE_X.get(info, dir);

        // delete directory with extracted jars
        final String extName = pkg.name().replaceAll("^.*\\.", "");
        final IOFile extDir = pkgf.parent().resolve('.' + extName);
        if(!extDir.delete()) throw BXRE_DELETE_X.get(info, extDir);
        deleted = true;
      }
    }
    if(!deleted) throw BXRE_WHICH_X.get(info, name);
  }

  /**
   * Returns a sorted list of all currently available packages.
   * @return packages
   */
  public ArrayList<Pkg> all() {
    final TreeMap<String, Pkg> map = new TreeMap<>();
    final EXPathRepo repo = context.repo.reset();
    final HashSet<String> cache = new HashSet<>();
    for(final Pkg pkg : repo.pkgDict().values()) {
      map.put(pkg.id(), pkg);
      cache.add(pkg.dir());
    }
    // ignore files and directories starting with dot (#1122)
    for(final IOFile ch : repo.path().children(DOT_FILE_FILTER)) {
      final String dir = ch.name();
      if(!ch.isDir()) {
        add(dir.replaceAll("\\..*", "").replace('/', '.'), dir, map);
      } else if(!cache.contains(dir)) {
        for(final String s : ch.descendants(DOT_FILE_FILTER)) {
          add(dir + '.' + s.replaceAll("\\..*", "").replace('/', '.'), dir + '/' + s, map);
        }
      }
    }
    return new ArrayList<>(map.values());
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Adds a package to the specified map.
   * @param name package name
   * @param dir package directory
   * @param map map
   */
  private static void add(final String name, final String dir, final TreeMap<String, Pkg> map) {
    final Pkg pkg = new Pkg();
    pkg.name = name;
    pkg.dir = dir;
    map.put(pkg.id(), pkg);
  }

  /**
   * Installs an XQuery module.
   * @param content package content
   * @param path package path
   * @return {@code true} if existing package was replaced
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private boolean installXQ(final byte[] content, final String path)
      throws QueryException, IOException {

    // parse module to find namespace uri
    try(QueryContext qc = new QueryContext(context)) {
      final byte[] uri = qc.parseLibrary(string(content), path, null).name.uri();
      // copy file to rewritten URI file path
      return write(Strings.uri2path(string(uri)) + IO.XQMSUFFIX, content);
    }
  }

  /**
   * Installs a JAR package.
   * @param path package path
   * @param content package content
   * @return {@code true} if existing package was replaced
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private boolean installJAR(final byte[] content, final String path)
      throws QueryException, IOException {

    final IOContent mf = new IOContent(new Zip(new IOContent(content)).read(MANIFEST_MF));
    final NewlineInput nli = new NewlineInput(mf);
    for(String s; (s = nli.readLine()) != null;) {
      // write file to rewritten file path
      final Matcher m = MAIN_CLASS.matcher(s);
      if(m.find()) return write(m.group(1).replace('.', '/') + IO.JARSUFFIX, content);
    }
    throw BXRE_MAIN_X.get(info, path);
  }

  /**
   * Writes a package to disk.
   * @param path file path
   * @param content package content
   * @return {@code true} if existing package was replaced
   * @throws IOException I/O exception
   */
  private boolean write(final String path, final byte[] content) throws IOException {
    final IOFile rp = new IOFile(context.soptions.get(StaticOptions.REPOPATH));
    final IOFile target = new IOFile(rp, path);
    final boolean exists = target.exists();
    target.parent().md();
    target.write(content);

    // extract JARs in zipped .lib directory
    if(target.hasSuffix(IO.JARSUFFIX)) {
      final IOFile extDir = target.parent().resolve('.' + target.name().
          replaceAll(IO.JARSUFFIX + '$', ""));
      try(JarFile jarFile = new JarFile(target.file())) {
        final Enumeration<JarEntry> entries = jarFile.entries();
        while(entries.hasMoreElements()) {
          final JarEntry entry = entries.nextElement();
          final String name = entry.getName();
          if(name.matches("^lib/[^/]+\\.jar")) {
            if(!extDir.md()) throw new BaseXException("Could not create %.", extDir.path());
            final IOFile file = new IOFile(extDir, name.replaceAll("^.*?/", ""));
            try(InputStream is = jarFile.getInputStream(entry)) {
              file.write(is);
            }
          }
        }
      }
    }
    return exists;
  }

  /**
   * Installs a XAR package.
   * @param content package content
   * @return {@code true} if existing package was replaced
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private boolean installXAR(final byte[] content) throws QueryException, IOException {
    final Zip zip = new Zip(new IOContent(content));
    // parse and validate descriptor file
    final IOContent dsc = new IOContent(zip.read(DESCRIPTOR));
    final Pkg pkg = new PkgParser(info).parse(dsc);

    // remove existing package
    final String id = pkg.id();
    final EXPathRepo repo = context.repo;
    final boolean exists = repo.pkgDict().get(id) != null;
    if(exists) delete(id);
    new PkgValidator(repo, info).check(pkg);

    // choose unique directory, unzip files and register repository
    final IOFile file = uniqueDir(id.replaceAll("[^\\w.-]+", "-"));
    zip.unzip(file);
    pkg.dir = file.name();
    repo.add(pkg);
    return exists;
  }

  /**
   * Returns a unique directory for the specified package.
   * @param name name
   * @return unique directory
   */
  private IOFile uniqueDir(final String name) {
    String nm = name;
    int c = 0;
    do {
      final IOFile io = context.repo.path(nm);
      if(!io.exists()) return io;
      nm = name + '-' + ++c;
    } while(true);
  }

  /**
   * Checks if a package participates in a dependency.
   * @param p package
   * @return package depending on the current one, or {@code null}
   * @throws QueryException query exception
   */
  private String primary(final Pkg p) throws QueryException {
    final String id = p.id();
    final EXPathRepo repo = context.repo;
    final HashMap<String, Pkg> dict = repo.pkgDict();
    for(final Pkg pkg : dict.values()) {
      if(!pkg.id().equals(id)) {
        // check only packages different from the current one
        final IOFile desc = new IOFile(repo.path(pkg.dir()), DESCRIPTOR);
        final String n = p.name();
        for(final PkgDep dep : new PkgParser(info).parse(desc).dep) {
          // check only package dependencies
          if(n.equals(dep.name)) return pkg.name();
        }
      }
    }
    return null;
  }
}
