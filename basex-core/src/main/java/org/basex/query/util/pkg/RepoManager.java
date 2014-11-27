package org.basex.query.util.pkg;

import static org.basex.core.Text.*;
import static org.basex.query.QueryError.*;
import static org.basex.query.util.pkg.PkgText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.util.pkg.Package.Dependency;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Repository manager.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Rositsa Shadura
 */
public final class RepoManager {
  /** Main-class pattern. */
  private static final Pattern MAIN_CLASS = Pattern.compile("^Main-Class: *(.+?) *$");
  /** Context. */
  private final Context context;
  /** Input info. */
  private InputInfo info;

  /**
   * Constructor.
   * @param context database context
   */
  public RepoManager(final Context context) {
    this.context = context;
  }

  /**
   * Constructor.
   * @param context database context
   * @param info input info
   */
  public RepoManager(final Context context, final InputInfo info) {
    this(context);
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
      if(io.hasSuffix(IO.JARSUFFIX)) return installJAR(cont);
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

    final Repo repo = context.repo;
    final TokenMap pkg = repo.pkgDict();
    // traverse EXPath packages
    for(final byte[] p : pkg) {
      if(p != null) t.contents.add(entry(string(Package.name(p)),
          string(Package.version(p)), EXPATH, string(pkg.get(p))));
    }

    // traverse all directories, ignore root entries with dashes
    for(final IOFile ch : repo.path().children()) {
      final String n = ch.name();
      if(!ch.isDir()) {
        t.contents.add(entry(n.replaceAll("\\..*", "").
            replace('/', '.'), "-", INTERNAL, n));
      } else if(n.indexOf('-') == -1) {
        for(final String s : ch.descendants()) {
          t.contents.add(entry(n + '.' + s.replaceAll("\\..*", "").replace('/', '.'),
              "-", INTERNAL, n + '/' + s));
        }
      }
    }
    return t.sort();
  }

  /**
   * Adds a single table entry.
   * @param name package name
   * @param version package version
   * @param type package type
   * @param path package path
   * @return new entry
   */
  private static TokenList entry(final String name, final String version, final String type,
      final String path) {

    final TokenList tl = new TokenList();
    tl.add(name);
    tl.add(version);
    tl.add(type);
    tl.add(path);
    return tl;
  }

  /**
   * Returns a list of all packages.
   * @return packages
   */
  public StringList list() {
    final Repo repo = context.repo;
    final StringList sl = new StringList();
    // traverse EXPath packages
    for(final byte[] p : repo.pkgDict()) {
      if(p != null) sl.add(string(p));
    }

    // traverse all directories, ignore root entries with dashes
    for(final IOFile ch : repo.path().children()) {
      final String n = ch.name();
      if(!ch.isDir()) {
        sl.add(n.replaceAll("\\..*", "").replace('/', '.'));
      } else if(n.indexOf('-') == -1) {
        for(final String s : ch.descendants()) {
          sl.add(n + '.' + s.replaceAll("\\..*", "").replace('/', '.'));
        }
      }
    }
    return sl.sort(false);
  }

  /**
   * Removes a package from the package repository.
   * @param pkg package
   * @throws QueryException query exception
   */
  public void delete(final String pkg) throws QueryException {
    boolean found = false;
    final Repo repo = context.repo;
    final TokenMap dict = repo.pkgDict();
    final byte[] pp = token(pkg);
    for(final byte[] nextPkg : dict) {
      if(nextPkg == null) continue;
      // a package can be deleted by its name or the name suffixed with its version
      if(eq(nextPkg, pp) || eq(Package.name(nextPkg), pp)) {
        // check if package to be deleted participates in a dependency
        final byte[] primPkg = primary(nextPkg);
        if(primPkg != null) throw BXRE_DEP_X_X.get(info, string(primPkg), pkg);

        // clean package repository
        final IOFile f = repo.path(string(dict.get(nextPkg)));
        repo.delete(new PkgParser(info).parse(new IOFile(f, DESCRIPTOR)));
        // package does not participate in a dependency => delete it
        if(!f.delete()) throw BXRE_DELETE_X.get(info, f);
        found = true;
      }
    }

    // traverse all files
    final IOFile file = file(pkg, repo);
    if(file != null) {
      if(!file.delete()) throw BXRE_DELETE_X.get(info, file);
      return;
    }

    if(!found) throw BXRE_WHICH_X.get(info, pkg);
  }

  /**
   * Looks for a file with the specified name.
   * @param name name
   * @param repo repository
   * @return file or {@code null}
   */
  public static IOFile file(final String name, final Repo repo) {
    // traverse all files, find exact matches
    IOFile path = new IOFile(repo.path(), name);
    for(final IOFile ch : path.parent().children()) {
      if(ch.name().equals(path.name())) return ch;
    }
    // traverse all files, find prefix matches
    path = new IOFile(repo.path(), name.replace('.', '/'));
    final String start = path.name() + '.';
    for(final IOFile ch : path.parent().children()) {
      if(ch.name().startsWith(start)) return ch;
    }
    return null;
  }

  // PRIVATE METHODS ====================================================================

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
    try(final QueryContext qc = new QueryContext(context)) {
      final byte[] uri = qc.parseLibrary(string(content), path, null).name.uri();
      // copy file to rewritten URI file path
      final String uriPath = ModuleLoader.uri2path(string(uri));
      if(uriPath == null) throw BXRE_URI_X.get(info, uri);
      return write(uriPath + IO.XQMSUFFIX, content);
    }
  }

  /**
   * Installs a JAR package.
   * @param content package content
   * @return {@code true} if existing package was replaced
   * @throws IOException I/O exception
   */
  private boolean installJAR(final byte[] content) throws IOException {
    final Zip zip = new Zip(new IOContent(content));
    final IOContent mf = new IOContent(zip.read(MANIFEST_MF));
    final NewlineInput nli = new NewlineInput(mf);
    for(String s; (s = nli.readLine()) != null;) {
      // write file to rewritten file path
      final Matcher m = MAIN_CLASS.matcher(s);
      if(m.find()) return write(m.group(1).replace('.', '/') + IO.JARSUFFIX, content);
    }
    return false;
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
    final Package pkg = new PkgParser(info).parse(dsc);

    // remove existing package
    final byte[] name = pkg.uniqueName();
    final Repo repo = context.repo;
    final boolean exists = repo.pkgDict().get(name) != null;
    if(exists) delete(string(name));
    new PkgValidator(repo, info).check(pkg);

    // choose unique directory, unzip files and register repository
    final IOFile file = uniqueDir(string(name).replaceAll("[^\\w.-]+", "-"));
    zip.unzip(file);
    repo.add(pkg, file.name());
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
   * @param pkgName package
   * @return package depending on the current one
   * @throws QueryException query exception
   */
  private byte[] primary(final byte[] pkgName) throws QueryException {
    final Repo repo = context.repo;
    final TokenMap dict = repo.pkgDict();
    for(final byte[] nextPkg : dict) {
      if(nextPkg != null && !eq(nextPkg, pkgName)) {
        // check only packages different from the current one
        final IOFile desc = new IOFile(repo.path(string(dict.get(nextPkg))), DESCRIPTOR);
        final Package pkg = new PkgParser(info).parse(desc);
        final byte[] name = Package.name(pkgName);
        for(final Dependency dep : pkg.dep)
          // Check only package dependencies
          if(dep.pkg != null && eq(dep.pkg, name)) return Package.name(nextPkg);
      }
    }
    return null;
  }
}
