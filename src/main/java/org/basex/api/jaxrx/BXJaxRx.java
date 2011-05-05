package org.basex.api.jaxrx;

import static org.basex.core.Text.*;
import static org.jaxrx.core.JaxRxConstants.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import javax.ws.rs.core.StreamingOutput;
import org.basex.core.BaseXException;
import org.basex.core.cmd.Delete;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.List;
import org.basex.core.cmd.ListDB;
import org.basex.core.cmd.Open;
import org.basex.data.SerializerProp;
import org.basex.data.XMLSerializer;
import org.basex.io.ArrayOutput;
import org.basex.io.IO;
import org.basex.server.ClientSession;
import org.basex.util.Table;
import org.basex.util.Token;
import org.basex.util.TokenList;
import org.basex.util.Util;
import org.jaxrx.JaxRx;
import org.jaxrx.core.JaxRxException;
import org.jaxrx.core.QueryParameter;
import org.jaxrx.core.ResourcePath;

/**
 * This class offers an implementation of the JAX-RX interface.
 * It contains all methods which are necessary to answer GET, POST, PUT
 * and DELETE requests via JAX-RX.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 * @author Lukas Lewandowski
 */
public final class BXJaxRx implements JaxRx {
  /** Configuration: User. */
  public static final String USER = "org.basex.user";
  /** Configuration: Password. */
  public static final String PASSWORD = "org.basex.password";
  /** Configuration: Server port. */
  public static final String SERVERPORT = "org.basex.serverport";
  /** Configuration: JAX-RX path. */
  public static final String JAXRXPATH = "org.basex.jaxrxpath";
  /** Configuration: serializer options. */
  public static final String SERIALIZER = "org.jaxrx.parameter.output";

  @Override
  public HashSet<QueryParameter> getParameters() {
    final HashSet<QueryParameter> p = new HashSet<QueryParameter>();
    p.add(QueryParameter.COMMAND);
    p.add(QueryParameter.QUERY);
    p.add(QueryParameter.RUN);
    p.add(QueryParameter.COUNT);
    p.add(QueryParameter.OUTPUT);
    p.add(QueryParameter.START);
    p.add(QueryParameter.WRAP);
    p.add(QueryParameter.VAR);
    return p;
  }

  @Override
  public StreamingOutput get(final ResourcePath rp) {
    return new BXOutput(rp) {
      @Override
      String code() throws IOException {
        final XMLSerializer xml = new XMLSerializer(out,
            new SerializerProp(serial(rp)));

        if(rp.getDepth() != 0) {
          final String all = rp.getResourcePath();

          final Table table = new Table(exec(new ListDB(all), null));
          xml.openElement(Token.token(JAXRX + ":" + "database"),
              Token.token("name"), Token.token(rp.getResource(0)),
            Token.token("documents"), Token.token(table.contents.size()));

          for(final TokenList l : table.contents) {
            xml.emptyElement(Token.token(JAXRX + ":" + "document"),
                Token.token("path"), l.get(0),
                Token.token("nodes"), l.get(1));
          }
          xml.closeElement();
        } else {
          // retrieve list of databases
          final Table table = new Table(exec(new List(), null));
          for(final TokenList l : table.contents) {
            xml.emptyElement(Token.token(JAXRX + ":" + "database"),
                Token.token("name"), l.get(0),
                Token.token("documents"), l.get(1),
                Token.token("size"), l.get(2));
          }
        }
        xml.close();
        return null;
      }
    };
  }

  @Override
  public StreamingOutput query(final String query, final ResourcePath rp) {
    return new BXOutput(rp) {
      @Override
      String code() {
        return query(query);
      }
    };
  }

  @Override
  public StreamingOutput run(final String file, final ResourcePath rp) {
    return new BXOutput(rp) {
      @Override
      String code() {
        // get root directory for files
        final String root = System.getProperty(JAXRXPATH) + "/";
        final IO io = IO.get(root + file);

        // file not found...
        if(!io.exists()) {
          throw new JaxRxException(404, Util.info(FILEWHICH, file));
        }

        try {
          // perform query
          return query(Token.string(io.content()));
        } catch(final IOException ex) {
          // file could not be opened for some other reason...
          throw new JaxRxException(400, ex.getMessage());
        }
      }
    };
  }

  @Override
  public StreamingOutput command(final String cmd, final ResourcePath rp) {
    return new BXOutput(rp) {
      @Override
      String code() throws IOException {
        // perform command
        final ArrayOutput ao = new ArrayOutput();
        exec(cmd, ao);

        // serialize output and remove carriage returns
        final XMLSerializer xml =
          new XMLSerializer(out, new SerializerProp(serial(path)));
        xml.text(Token.delete(ao.toArray(), '\r'));
        xml.close();
        return cs.info();
      }
    };
  }

  @Override
  public String add(final InputStream input, final ResourcePath rp) {
    return new BXCode(rp) {
      @Override
      String code() {
        // open database
        try {
          cs.execute(new Open(db(rp)));
        } catch(final BaseXException ex) {
          throw new JaxRxException(404, ex.getMessage());
        }
        add(input, rp, cs);
        return cs.info();
      }
    }.run();
  }

  /**
   * Adds a document to the database.
   * @param input input stream
   * @param rp resource path
   * @param cs client session
   */
  void add(final InputStream input, final ResourcePath rp,
      final ClientSession cs) {

    final int d = rp.getDepth();
    final String name = rp.getResource(d - 1);
    final StringBuilder target = new StringBuilder();
    for(int i = 1; i < d - 1; i++) {
      target.append('/').append(rp.getResource(i));
    }
    try {
      cs.add(name, target.toString(), input);
    } catch(final BaseXException ex) {
      throw new JaxRxException(400, ex.getMessage());
    }
  }

  @Override
  public String update(final InputStream input, final ResourcePath rp) {
    return new BXCode(rp) {
      @Override
      String code() {
        try {
          final int d = rp.getDepth();
          // create new database
          if(d == 1) {
            cs.create(db(rp), input);
          } else {
            // add document to database
            cs.execute(new Open(db(rp)));
            final StringBuilder target = new StringBuilder();
            for(int i = 1; i < d; i++) {
              target.append('/').append(rp.getResource(i));
            }
            cs.execute(new Delete(target.toString()));
            add(input, rp, cs);
          }
          return cs.info();
        } catch(final BaseXException ex) {
          // return exception if process failed
          throw new JaxRxException(400, ex.getMessage());
        }
      }
    }.run();
  }

  @Override
  public String delete(final ResourcePath rp) {
    return new BXCode(rp) {
      @Override
      String code() {
        final boolean root = rp.getDepth() == 1;
        try {
          if(root) {
            cs.execute(new DropDB(db(rp)));
          } else {
            cs.execute(new Open(db(rp)));
            cs.execute(new Delete(path(rp)));
          }
          return cs.info();
        } catch(final BaseXException ex) {
          // return exception if process failed
          if(root) throw new JaxRxException(ex);
          throw new JaxRxException(404, ex.getMessage());
        }
      }
    }.run();
  }
}
