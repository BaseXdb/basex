package org.basex.query.func.zip;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Functions on zip files.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class ZipUpdateEntries extends ZipZipFile {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // check argument
    final ANode elm = toElem(exprs[0], qc);
    if(!elm.qname().eq(Q_FILE)) throw ZIP_UNKNOWN_X.get(info, elm.qname());

    // sorted paths in original file
    final String in = attribute(elm, HREF, true);

    // target and temporary output file
    final IOFile target = new IOFile(string(toToken(exprs[1], qc)));
    IOFile out;
    do {
      out = new IOFile(target.path() + new Random().nextInt(0x7FFFFFFF));
    } while(out.exists());

    // open zip file
    if(!new IOFile(in).exists()) throw ZIP_NOTFOUND_X.get(info, in);
    boolean ok = true;
    try(final ZipFile zf = new ZipFile(in)) {
      // write zip file
      try(final FileOutputStream fos = new FileOutputStream(out.path());
          final ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos))) {
        // fill new zip file with entries from old file and description
        create(zos, elm.children(), "", zf, qc);
      } catch(final IOException ex) {
        ok = false;
        throw ZIP_FAIL_X.get(info, ex);
      }
    } catch(final IOException ex) {
      throw ZIP_FAIL_X.get(info, ex);
    } finally {
      if(ok) {
        // rename temporary file to final target
        target.delete();
        out.rename(target);
      } else {
        // remove temporary file
        out.delete();
      }
    }
    return null;
  }
}
