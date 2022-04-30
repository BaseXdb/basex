package org.basex.query.func.zip;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.zip.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Functions on zip files.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public class ZipBinaryEntry extends ZipFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return B64.get(entry(qc));
  }

  /**
   * Returns an entry from a zip file.
   * @param qc query context
   * @return binary result
   * @throws QueryException query exception
   */
  final byte[] entry(final QueryContext qc) throws QueryException {
    final IOFile file = new IOFile(toString(exprs[0], qc));
    final String path = toString(exprs[1], qc);
    if(!file.exists()) throw ZIP_NOTFOUND_X.get(info, file);

    try(ZipInputStream in = new ZipInputStream(file.inputStream())) {
      final byte[] cont = getEntry(in, path);
      if(cont == null) throw new FileNotFoundException(path);
      return cont;
    } catch(final FileNotFoundException ex) {
      Util.debug(ex);
      throw ZIP_NOTFOUND_X.get(info, file + "/" + path);
    } catch(final IOException ex) {
      throw ZIP_FAIL_X.get(info, ex);
    }
  }

  /**
   * Returns the contents of the specified entry.
   * @param in input stream
   * @param entry entry to be found
   * @return entry, or {@code null} if it is not found
   * @throws IOException I/O exception
   */
  private static byte[] getEntry(final ZipInputStream in, final String entry) throws IOException {
    for(ZipEntry ze; (ze = in.getNextEntry()) != null;) {
      if(!entry.equals(ze.getName())) continue;
      final int s = (int) ze.getSize();
      if(s >= 0) {
        // known size: pre-allocate and fill array
        final byte[] data = new byte[s];
        int c, o = 0;
        while(s - o != 0 && (c = in.read(data, o, s - o)) != -1) o += c;
        return data;
      }
      // unknown size: use byte list
      final byte[] data = new byte[IO.BLOCKSIZE];
      final ByteList bl = new ByteList();
      for(int c; (c = in.read(data)) != -1;) bl.add(data, 0, c);
      return bl.finish();
    }
    return null;
  }
}
