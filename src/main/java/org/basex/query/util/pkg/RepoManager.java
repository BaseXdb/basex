package org.basex.query.util.pkg;

import static org.basex.core.Text.*;
import static org.basex.query.util.Err.*;
import static org.basex.query.util.pkg.PkgText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.util.pkg.Package.Dependency;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Repository manager.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public final class RepoManager {
  /** Main-class pattern. */
  private static final Pattern MAIN_CLASS = Pattern.compile("^Main-Class: *(.+?) *$");
  /** Repository context. */
  private final Repo repo;
  /** Input info. */
  private InputInfo info;

  /**
   * Constructor.
   * @param ctx database context
   */
  public RepoManager(final Context ctx) {
    repo = ctx.repo;
  }

  /**
   * Constructor.
   * @param ctx database context
   * @param ii input info
   */
  public RepoManager(final Context ctx, final InputInfo ii) {
    this(ctx);
    info = ii;
  }

  /**
   * Installs a package.
   * @param path package path
   * @return {@code true} if package was replaced
   * @throws QueryException query exception
   */
  public boolean install(final byte[] path) throws QueryException {
    // check if package exists, and cache contents
    final IO io = IO.get(string(path));
    byte[] cont = null;
    try {
      cont = io.read();
    } catch(final IOException ex) {
      Util.debug(ex);
      PKGNOTEXIST.thrw(info, path);
    }

    try {
      if(io.hasSuffix(IO.XQSUFFIXES)) return installXQ(cont);
      if(io.hasSuffix(IO.JARSUFFIX)) return installJAR(cont);
      return installXAR(cont);
    } catch(final IOException ex) {
      final Err err = ex instanceof FileNotFoundException ? PKGREADFNF : PKGREADFAIL;
      Util.debug(ex);
      throw err.thrw(info, io.name(), ex.getMessage());
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
    t.header.add(DIRECTORY);

    final TokenMap pkg = repo.pkgDict();
    // traverse EXPath packages
    for(final byte[] p : pkg) {
      if(p == null) continue;
      final TokenList tl = new TokenList();
      tl.add(Package.name(p));
      tl.add(Package.version(p));
      tl.add(EXPATH);
      tl.add(pkg.get(p));
      t.contents.add(tl);
    }

    // traverse all directories, ignore root entries with dashes
    for(final IOFile dir : repo.path().children()) {
      if(dir.name().indexOf('-') != -1) continue;
      for(final String s : dir.descendants()) {
        final TokenList tl = new TokenList();
        tl.add(dir.name() + '.' + s.replaceAll("\\..*", "").replace('/', '.'));
        tl.add("-");
        tl.add(INTERNAL);
        tl.add(dir.name() + '/' + s);
        t.contents.add(tl);
      }
    }

    t.sort();
    return t;
  }

  /**
   * Returns a list of all packages.
   * @return packages
   */
  public StringList list() {
    final StringList sl = new StringList();
    // traverse EXPath packages
    for(final byte[] p : repo.pkgDict()) {
      if(p != null) sl.add(Token.string(p));
    }

    // traverse all directories, ignore root entries with dashes
    for(final IOFile dir : repo.path().children()) {
      if(dir.name().indexOf('-') != -1) continue;
      for(final String s : dir.descendants()) {
        sl.add(dir.name() + '.' + s.replaceAll("\\..*", "").replace('/', '.'));
      }
    }

    sl.sort(false, true);
    return sl;
  }

  /**
   * Removes a package from the package repository.
   * @param pkg package
   * @throws QueryException query exception
   */
  public void delete(final byte[] pkg) throws QueryException {
    boolean found = false;
    final TokenMap dict = repo.pkgDict();
    for(final byte[] nextPkg : dict) {
      if(nextPkg == null) continue;
      // a package can be deleted by its name or the name suffixed with its version
      if(eq(nextPkg, pkg) || eq(Package.name(nextPkg), pkg)) {
        // check if package to be deleted participates in a dependency
        final byte[] primPkg = primary(nextPkg);
        if(primPkg != null) PKGDEP.thrw(info, string(primPkg), pkg);

        // clean package repository
        final IOFile f = repo.path(string(dict.get(nextPkg)));
        repo.delete(new PkgParser(repo, info).parse(new IOFile(f, DESCRIPTOR)));
        // package does not participate in a dependency => delete it
        if(!f.delete()) PKGDEL.thrw(info, f);
        found = true;
      }
    }

    // traverse all files
    final IOFile file = file(pkg, repo);
    if(file != null) {
      if(!file.delete()) PKGDEL.thrw(info, file);
      return;
    }

    if(!found) PKGNOTEXIST.thrw(info, pkg);
  }

  /**
   * Looks for a file with the specified name.
   * @param name name
   * @param repo repository
   * @return file, or {@code null}
   */
  public static IOFile file(final byte[] name, final Repo repo) {
    // traverse all files, find exact matches
    final String nm = string(name);
    IOFile path = new IOFile(repo.path(), nm);
    for(final IOFile ch : new IOFile(path.dir()).children()) {
      if(ch.name().equals(path.name())) return ch;
    }
    // traverse all files, find prefix matches
    path = new IOFile(repo.path(), nm.replace('.', '/'));
    final String start = path.name() + '.';
    for(final IOFile ch : new IOFile(path.dir()).children()) {
      if(ch.name().startsWith(start)) return ch;
    }
    return null;
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Installs an XQuery module.
   * @param cont package content
   * @return {@code true} if existing package was replaced
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private boolean installXQ(final byte[] cont) throws QueryException, IOException {
    // parse module to find namespace uri
    final Context ctx = repo.context;
    final byte[] uri = new QueryContext(ctx).module(string(cont)).uri();

    // copy file to rewritten URI file path
    final String path = ModuleLoader.uri2path(string(uri));
    if(path == null) INSTERR.thrw(info, uri);

    final IOFile rp = new IOFile(ctx.mprop.get(MainProp.REPOPATH));
    final boolean exists = md(rp, path);
    new IOFile(rp, path + IO.XQMSUFFIX).write(cont);
    return exists;
  }

  /**
   * Installs a JAR package.
   * @param cont package content
   * @return {@code true} if existing package was replaced
   * @throws IOException I/O exception
   */
  private boolean installJAR(final byte[] cont) throws IOException {
    final Zip zip = new Zip(new IOContent(cont));
    final IOContent mf = new IOContent(zip.read(MANIFEST_MF));
    final NewlineInput nli = new NewlineInput(mf);
    for(String l; (l = nli.readLine()) != null;) {
      final Matcher m = MAIN_CLASS.matcher(l);
      if(!m.find()) continue;

      // copy file to rewritten file path
      final IOFile rp = new IOFile(repo.context.mprop.get(MainProp.REPOPATH));
      final String path = m.group(1).replace('.', '/');
      final boolean exists = md(rp, path);
      new IOFile(rp, path + IO.JARSUFFIX).write(cont);
      return exists;
    }
    return false;
  }

  /**
   * Creates the package directory and deletes existing packages.
   * @param rp path to the repository
   * @param path file path
   * @return {@code true} if a package already existed
   */
  private boolean md(final IOFile rp, final String path) {
    final IOFile target = new IOFile(rp, path);
    final IOFile dir = new IOFile(target.dir());
    dir.md();
    final IOFile[] ch = dir.children(target.name() + ".*");
    for(final IOFile c : ch) c.delete();
    return ch.length != 0;
  }

  /**
   * Installs a XAR package.
   * @param cont package content
   * @return {@code true} if existing package was replaced
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private boolean installXAR(final byte[] cont) throws QueryException, IOException {
    final Zip zip = new Zip(new IOContent(cont));
    // parse and validate descriptor file
    final IOContent dsc = new IOContent(zip.read(DESCRIPTOR));
    final Package pkg = new PkgParser(repo, info).parse(dsc);

    // remove existing package
    final byte[] name = pkg.uniqueName();
    final boolean exists = repo.pkgDict().get(name) != null;
    if(exists) delete(name);
    new PkgValidator(repo, info).check(pkg);

    // choose unique directory, unzip files and register repository
    final IOFile file = uniqueDir(string(name).replaceAll("[^\\w.-]+", "-"));
    zip.unzip(file);
    repo.add(pkg, file.name());
    return exists;
  }

  /**
   * Returns a unique directory for the specified package.
   * @param n name
   * @return unique directory
   */
  private IOFile uniqueDir(final String n) {
    String nm = n;
    int c = 0;
    do {
      final IOFile io = repo.path(nm);
      if(!io.exists()) return io;
      nm = n + '-' + ++c;
    } while(true);
  }

  /**
   * Checks if a package participates in a dependency.
   * @param pkgName package
   * @return package depending on the current one
   * @throws QueryException query exception
   */
  private byte[] primary(final byte[] pkgName) throws QueryException {
    final TokenMap dict = repo.pkgDict();
    for(final byte[] nextPkg : dict) {
      if(nextPkg != null && !eq(nextPkg, pkgName)) {
        // check only packages different from the current one
        final IOFile desc = new IOFile(repo.path(string(dict.get(nextPkg))), DESCRIPTOR);
        final Package pkg = new PkgParser(repo, info).parse(desc);
        final byte[] name = Package.name(pkgName);
        for(final Dependency dep : pkg.dep)
          // Check only package dependencies
          if(dep.pkg != null && eq(dep.pkg, name)) return Package.name(nextPkg);
      }
    }
    return null;
  }
}
