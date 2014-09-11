package org.basex.query.func.zip;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Functions on zip files.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ZipEntries extends ZipFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String file = string(toToken(exprs[0], qc));

    // check file path
    final IOFile path = new IOFile(file);
    if(!path.exists()) throw ZIP_NOTFOUND_X.get(info, file);
    // loop through file
    try(final ZipFile zf = new ZipFile(file)) {
      // create result node
      final FElem root = new FElem(Q_FILE).declareNS().add(HREF, path.path());
      createEntries(paths(zf).iterator(), root, "");
      return root;
    } catch(final IOException ex) {
      throw ZIP_FAIL_X.get(info, ex);
    }
  }

  /**
   * Creates the zip archive nodes in a recursive manner.
   * @param it iterator
   * @param par parent node
   * @param pref directory prefix
   * @return current prefix
   */
  private static String createEntries(final Iterator<String> it, final FElem par,
      final String pref) {

    String path = null;
    boolean curr = false;
    while(curr || it.hasNext()) {
      if(!curr) {
        path = it.next();
        curr = true;
      }
      if(path == null) break;
      // current entry is located in a higher/other directory
      if(!path.startsWith(pref)) return path;

      // current file starts with new directory
      final int i = path.lastIndexOf('/');
      final String dir = i == -1 ? path : path.substring(0, i);
      final String name = path.substring(i + 1);

      if(name.isEmpty()) {
        // path ends with slash: create directory
        path = createEntries(it, createDir(par, dir), dir);
      } else {
        // create file
        createFile(par, name);
        curr = false;
      }
    }
    return null;
  }

  /**
   * Creates a directory element.
   * @param par parent node
   * @param name name of directory
   * @return element
   */
  private static FElem createDir(final FElem par, final String name) {
    final FElem e = new FElem(Q_DIR).add(NAME, name);
    par.add(e);
    return e;
  }

  /**
   * Creates a file element.
   * @param par parent node
   * @param name name of directory
   */
  private static void createFile(final FElem par, final String name) {
    par.add(new FElem(Q_ENTRY).add(NAME, name));
  }

  /**
   * Returns a list of all file paths.
   * @param zf zip file file to be parsed
   * @return binary result
   */
  private static StringList paths(final ZipFile zf) {
    // traverse all zip entries and create intermediate map,
    // as zip entries are not sorted
    //final StringList paths = new StringList();
    final TreeSet<String> paths = new TreeSet<>();

    final Enumeration<? extends ZipEntry> en = zf.entries();
    // loop through all files
    while(en.hasMoreElements()) {
      final ZipEntry ze = en.nextElement();
      final String name = ze.getName();
      final int i = name.lastIndexOf('/');
      // add directory
      if(i > -1 && i + 1 < name.length()) paths.add(name.substring(0, i + 1));
      paths.add(name);
    }
    final StringList sl = new StringList();
    for(final String path : paths) sl.add(path);
    return sl;
  }
}
