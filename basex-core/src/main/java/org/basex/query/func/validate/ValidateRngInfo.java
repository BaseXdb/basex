package org.basex.query.func.validate;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.lang.reflect.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.util.*;
import org.xml.sax.*;

/**
 * Validates a document against a RelaxNG document.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public class ValidateRngInfo extends ValidateFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return info(qc);
  }

  @Override
  public Value info(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    return process(new Validate() {
      @Override
      void process(final ErrorHandler handler) throws IOException, SAXException, QueryException {
        final IO in = read(toNodeOrAtomItem(exprs[0], qc), qc, null);

        // schema specified as string
        IO schema = read(toNodeOrAtomItem(exprs[1], qc), qc, null);
        tmp = createTmp(schema);
        if(tmp != null) schema = tmp;

        try {
          final Class<?> pmb = Class.forName("com.thaiopensource.util.PropertyMapBuilder");
          final Class<?> vd = Class.forName("com.thaiopensource.validate.ValidationDriver");
          final Class<?> vp = Class.forName("com.thaiopensource.validate.ValidateProperty");
          final Class<?> pi = Class.forName("com.thaiopensource.util.PropertyId");
          final Class<?> pm = Class.forName("com.thaiopensource.util.PropertyMap");

          final Object ehInstance = vp.getField("ERROR_HANDLER").get(null);
          final Object pmbInstance = pmb.newInstance();
          pi.getMethod("put", pmb, Object.class).invoke(ehInstance, pmbInstance, handler);

          final Object pmInstance = pmb.getMethod("toPropertyMap").invoke(pmbInstance);
          final Object vdInstance = vd.getConstructor(pm).newInstance(pmInstance);

          final Method vdLs = vd.getMethod("loadSchema", InputSource.class);
          final Object loaded = vdLs.invoke(vdInstance, schema.inputSource());
          if(!Boolean.TRUE.equals(loaded)) return;

          final Method vdV = vd.getMethod("validate", InputSource.class);
          vdV.invoke(vdInstance, in.inputSource());

        } catch(final Exception ex) {
          Throwable e = ex;
          while(e.getCause() != null) {
            Util.debug(e);
            e = e.getCause();
          }
          throw BXVA_RNG_X.get(info, e);
        }
      }
    });
  }
}
