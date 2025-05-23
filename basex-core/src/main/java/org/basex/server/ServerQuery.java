package org.basex.server;

import static org.basex.core.Text.*;
import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.api.client.*;
import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Server query representation, generated by {@link LocalQuery} and {@link ClientQuery}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ServerQuery extends Job {
  /** Query string. */
  private final String query;
  /** Database context. */
  private final Context ctx;

  /** Query processor. */
  private QueryProcessor qp;
  /** Parsing flag. */
  private boolean parsed;
  /** Query info. */
  private final StringBuilder info = new StringBuilder();

  /**
   * Constructor.
   * @param query query string
   * @param ctx database context
   */
  public ServerQuery(final String query, final Context ctx) {
    this.query = query;
    this.ctx = ctx;
  }

  /**
   * Binds a global variable.
   * @param name name of variable
   * @param value value to be bound
   * @param type type
   * @throws IOException query exception
   */
  public void bind(final String name, final Object value, final String type) throws IOException {
    try {
      qp().variable(name, value, type);
    } catch(final QueryException ex) {
      throw new BaseXException(ex);
    }
  }

  /**
   * Binds the context value.
   * @param value value to be bound
   * @param type type
   * @throws IOException query exception
   */
  public void context(final Object value, final String type) throws IOException {
    try {
      qp().context(value, type);
    } catch(final QueryException ex) {
      throw new BaseXException(ex);
    }
  }

  /**
   * Returns the query info.
   * @return query info
   */
  public String info() {
    return info.toString();
  }

  /**
   * Returns the serialization parameters.
   * @return serialization parameters
   * @throws IOException I/O Exception
   */
  public String parameters() throws IOException {
    parse();
    return qp.qc.parameters().toString();
  }

  /**
   * Returns {@code true} if the query may perform updates.
   * @return updating flag
   * @throws IOException I/O Exception
   */
  public boolean updating() throws IOException {
    parse();
    return qp.updating;
  }

  /**
   * Executes the query.
   * @param out output stream
   * @param iterative iterative evaluation
   * @param encode encode results (client/server communication, iterative processing)
   * @param full return full type information (only applicable to iterative evaluation)
   * @throws IOException I/O Exception
   */
  public void execute(final OutputStream out, final boolean iterative, final boolean encode,
      final boolean full) throws IOException {

    try {
      // parses the query and registers the process
      parse();
      qp.register(ctx);

      final QueryContext qc = qp.qc;
      final QueryInfo qi = qc.info;

      qp.optimize();
      final Iter iter = qp.iter();

      // iterate through results
      int hits = 0;
      final PrintOutput po = PrintOutput.get(encode ? new ServerOutput(out) : out);
      final SerializerOptions sopts = full ? SerializerMode.API.get() : qc.parameters();
      try(Serializer ser = Serializer.get(po, sopts)) {
        for(Item item; (item = qc.next(iter)) != null;) {
          if(iterative) {
            if(item.type instanceof EnumType) item = Str.get(item.string(null));
            if(full) po.write(item.xdmInfo());
            else po.write(item.typeId().asByte());
            ser.reset();
            ser.serialize(item);
            po.flush();
            out.write(0);
          } else {
            ser.serialize(item);
          }
          hits++;
        }
      }

      // generate query info
      info.append(qi.toString(qp, po.size(), hits, jc().locks, true));
    } catch(final QueryException | JobException ex) {
      throw new BaseXException(ex);
    } catch(final StackOverflowError ex) {
      Util.debug(ex);
      throw new BaseXException(BASEX_OVERFLOW.message());
    } finally {
      // close processor and unregisters the process
      if(qp != null) {
        if(parsed) {
          qp.close();
          qp.unregister(ctx);
          parsed = false;
        }
        qp = null;
        popJob();
      }
    }
  }

  /**
   * Generates a query plan.
   * @throws QueryIOException query I/O exception
   */
  private void queryPlan() throws QueryIOException {
    if(ctx.options.get(MainOptions.XMLPLAN)) {
      info.append(NL).append(QUERY_PLAN).append(COL).append(NL);
      info.append(qp.toXml().serialize(SerializerMode.INDENT.get())).append(NL);
    }
  }

  /**
   * Initializes the query.
   * @throws IOException I/O Exception
   */
  private void parse() throws IOException {
    if(parsed) return;

    try {
      qp().parse();
      qp.compile();
      queryPlan();
    } catch(final QueryException ex) {
      throw new BaseXException(ex);
    }
    parsed = true;
  }

  /**
   * Initializes and returns an instance of the query processor.
   * @return query processor
   */
  private QueryProcessor qp() {
    if(parsed || qp == null) {
      qp = pushJob(new QueryProcessor(query, ctx));
      parsed = false;
    }
    return qp;
  }
}
