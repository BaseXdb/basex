package org.basex.query.func.validate;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.xml.sax.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public class ValidateRng extends ValidateFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return check(qc);
  }

  @Override
  public ArrayList<ErrorInfo> errors(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    return process(new Validation() {
      @Override
      void process(final ValidationHandler handler) throws IOException, QueryException {
        final IO in = read(toNodeOrAtomItem(exprs[0], qc), null);
        final Item sch = toNodeOrAtomItem(exprs[1], qc);
        final boolean compact = exprs.length > 2 && toBoolean(exprs[2], qc);

        // detect format of schema input
        IO schema;
        try {
          schema = read(sch, null);
        } catch(final QueryException ex) {
          // compact schema: treat string as input
          if(!compact || ex.error() != WHICHRES_X) throw ex;
          schema = new IOContent(sch.string(info));
        }
        schema = prepare(schema, handler);

        try {
          final Class<?>
            pmb = Class.forName("com.thaiopensource.util.PropertyMapBuilder"),
            vd = Class.forName("com.thaiopensource.validate.ValidationDriver"),
            vp = Class.forName("com.thaiopensource.validate.ValidateProperty"),
            pi = Class.forName("com.thaiopensource.util.PropertyId"),
            pm = Class.forName("com.thaiopensource.util.PropertyMap"),
            sr = Class.forName("com.thaiopensource.validate.SchemaReader"),
            csr = Class.forName("com.thaiopensource.validate.rng.CompactSchemaReader");

          final Object ehInstance = vp.getField("ERROR_HANDLER").get(null);
          final Object pmbInstance = pmb.newInstance();
          pi.getMethod("put", pmb, Object.class).invoke(ehInstance, pmbInstance, handler);

          final Object srInstance = compact ? csr.getMethod("getInstance").invoke(null) : null;
          final Object pmInstance = pmb.getMethod("toPropertyMap").invoke(pmbInstance);
          final Object vdInstance = vd.getConstructor(pm, sr).newInstance(pmInstance, srInstance);

          final Method vdLs = vd.getMethod("loadSchema", InputSource.class);
          final Object loaded = vdLs.invoke(vdInstance, schema.inputSource());
          if(Boolean.TRUE.equals(loaded)) {
            final Method vdV = vd.getMethod("validate", InputSource.class);
            vdV.invoke(vdInstance, in.inputSource());
          }
        } catch(final ClassNotFoundException ex) {
          throw BXVA_RELAXNG_X.get(info);
        } catch(final Exception ex) {
          throw BXVA_FAIL_X.get(info, Util.rootException(ex));
        }
      }
    });
  }
}
