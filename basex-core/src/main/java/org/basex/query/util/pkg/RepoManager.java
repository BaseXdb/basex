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
 * @author BaseX Team 2005-21, BSD License
 * @author Rositsa Shadura
 */
public final class RepoManager {
  /** Main-class pattern. */
  private static final Pattern MAIN_CLASS = Pattern.compile("^Main-Class: *(.+?) *$");
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
   * @return {@code true} if existing package was replaced
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
      throw REPO_NOTFOUND_X.get(info, path);
    }

    try {
      if(io.hasSuffix(IO.XQSUFFIXES)) return installXQ(cont, path);
      if(io.hasSuffix(IO.JARSUFFIX)) return installJAR(cont, path);
      return installXAR(cont);
    } catch(final IOException ex) {
      throw REPO_PARSE_X_X.get(info, io.name(), ex);
    }
  }

  /**
   * Returns all installed packages in a table.
   * @return table
   */
  public Table table() {
    final Table table = new Table();
    table.description = PACKAGES_X;
    table.header.add(NAME);
    table.header.add(VERSINFO);
    table.header.add(TYPE);
    table.header.add(PATH);
    for(final Pkg pkg : packages()) {
      final TokenList tl = new TokenList();
      tl.add(pkg.name());
      tl.add(pkg.version());
      tl.add(pkg.type().toString());
      tl.add(pkg.path());
      table.contents.add(tl);
    }
    return table;
  }

  /**
   * Returns a list of all package ids.
   * @return packages
   */
  public StringList ids() {
    final StringList sl = new StringList();
    for(final Pkg pkg : packages()) sl.add(pkg.id());
    return sl;
  }

  /**
   * Removes a package from the repository.
   * @param name name or id of the package
   * @throws QueryException query exception
   */
  public void delete(final String name) throws QueryException {
    // find registered packages to be deleted
    boolean deleted = false;
    final EXPathRepo repo = context.repo;
    for(final Pkg pkg : packages()) {
      final String pkgPath = pkg.path();
      if(pkg.name().equals(name) || pkg.id().equals(name) || pkgPath.equals(name)) {
        if(pkg.type() == PkgType.EXPATH) {
          // check if package to be deleted participates in a dependency
          final String dep = dependency(pkg);
          if(dep != null) throw REPO_DELETE_X_X.get(info, dep, name);
          // delete files in main-memory repository
          repo.delete(pkg);
        }

        if(pkg.type() == PkgType.COMBINED) {
          // delete associated JAR file
          final IOFile pkgFile = repo.path(pkgPath.replaceAll("\\.[^.]+$", IO.JARSUFFIX));
          if(!pkgFile.delete()) throw REPO_DELETE_X.get(info, pkgPath);
        }

        // delete package directory or file
        final IOFile pkgFile = repo.path(pkgPath);
        if(!pkgFile.delete()) throw REPO_DELETE_X.get(info, pkgPath);

        // delete directory with extracted jars
        final IOFile extDir = pkgFile.parent().resolve('.' + pkg.name().replaceAll("^.*\\.", ""));
        if(!extDir.delete()) throw REPO_DELETE_X.get(info, extDir);
        deleted = true;
      }
    }
    if(!deleted) throw REPO_NOTFOUND_X.get(info, name);
  }

  /**
   * Returns a sorted list of all currently available packages.
   * @return packages
   */
  public ArrayList<Pkg> packages() {
    final TreeMap<String, Pkg> map = new TreeMap<>();
    final EXPathRepo repo = context.repo.reset();
    final HashSet<String> paths = new HashSet<>();
    for(final Pkg pkg : repo.pkgDict().values()) {
      add(pkg, map);
      paths.add(pkg.path());
    }
    // ignore files and directories starting with dot (#1122)
    for(final IOFile child : repo.path().children(IOFile.NO_HIDDEN)) {
      final String name = child.name();
      if(!child.isDir()) {
        add(name.replaceAll("\\..*", "").replace('/', '.'), name, map);
      } else if(!paths.contains(name)) {
        for(final String path : child.descendants(IOFile.NO_HIDDEN)) {
          add(name + '.' + path.replaceAll("\\..*", "").replace('/', '.'), name + '/' + path, map);
        }
      }
    }
    return new ArrayList<>(map.values());
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Adds a package to the specified map.
   * @param name package name
   * @param path path to the package
   * @param map map
   */
  private static void add(final String name, final String path, final TreeMap<String, Pkg> map) {
    add(new Pkg(name).path(path), map);
  }

  /**
   * Adds a package to the specified map.
   * @param pkg package
   * @param map map
   */
  private static void add(final Pkg pkg, final TreeMap<String, Pkg> map) {
    map.compute(pkg.id(), (k, v) -> v == null ? pkg : v.merge(pkg));
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
      final byte[] uri = qc.parseLibrary(string(content), path).sc.module.uri();
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

    final byte[] manifest = new Zip(new IOContent(content)).read(MANIFEST_MF);
    try(NewlineInput nli = new NewlineInput(manifest)) {
      for(String s; (s = nli.readLine()) != null;) {
        // write file to rewritten file path
        final Matcher main = MAIN_CLASS.matcher(s);
        if(main.find()) return write(main.group(1).replace('.', '/') + IO.JARSUFFIX, content);
      }
    }
    throw REPO_PARSE_X_X.get(info, path, MANIFEST);
  }

  /**
   * Writes a package to disk.
   * @param path file path
   * @param content package content
   * @return {@code true} if existing package was replaced
   * @throws IOException I/O exception
   */
  private boolean write(final String path, final byte[] content) throws IOException {
    final IOFile repo = new IOFile(context.soptions.get(StaticOptions.REPOPATH));
    final IOFile target = new IOFile(repo, path);
    final boolean exists = target.exists();
    if(!target.parent().md()) throw new BaseXException("Could not create %.", target);
    target.write(content);

    // extract files from JAR package
    if(target.hasSuffix(IO.JARSUFFIX)) {
      final String pkgPath = path.replaceAll(IO.JARSUFFIX + '$', "");
      final String pkgName = target.name().replaceAll(IO.JARSUFFIX + '$', "");
      try(JarFile jarFile = new JarFile(target.file())) {
        final Enumeration<JarEntry> entries = jarFile.entries();
        while(entries.hasMoreElements()) {
          final JarEntry entry = entries.nextElement();
          final String name = entry.getName();

          IOFile trg = null;
          if(name.matches("^lib/[^/]+\\.jar")) {
            // extract JARs from a zipped lib/ directory to the repository
            trg = new IOFile(target.parent().resolve('.' + pkgName), name.replaceAll("^.*?/", ""));
          } else if(name.equals(pkgPath + IO.XQMSUFFIX)) {
            // extract XQM file
            trg = new IOFile(repo, name);
          }
          if(trg != null) {
            if(!trg.parent().md()) throw new BaseXException("Could not create %.", trg);
            trg.write(jarFile.getInputStream(entry));
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

    // adds package to the repository after assigning its path
    repo.add(pkg.path(file.name()));
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
   * @param pkg package
   * @return package (that depends on the current one) or {@code null}
   * @throws QueryException query exception
   */
  private String dependency(final Pkg pkg) throws QueryException {
    final String id = pkg.id();
    final EXPathRepo repo = context.repo;
    final HashMap<String, Pkg> dict = repo.pkgDict();
    for(final Pkg pkgDep : dict.values()) {
      if(!pkgDep.id().equals(id)) {
        // check only packages different from the current one
        final IOFile desc = new IOFile(repo.path(pkgDep.path()), DESCRIPTOR);
        final String name = pkg.name();
        for(final PkgDep dep : new PkgParser(info).parse(desc).dep) {
          // check only package dependencies
          if(name.equals(dep.name)) return pkgDep.name();
        }
      }
    }
    return null;
  }
}
