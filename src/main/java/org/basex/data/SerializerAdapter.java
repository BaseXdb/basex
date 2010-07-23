package org.basex.data;

import java.io.IOException;

/**
 * This class subclasses {@link Serializer} and provides empty methods for all
 * abstract methods.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Josua Krause
 * @author Leo Woerteler
 */
@SuppressWarnings("unused")
public class SerializerAdapter extends Serializer {

  @Override
  public void attribute(final byte[] n, final byte[] v) throws IOException { }

  @Override
  protected void close(final byte[] t) throws IOException { }

  @Override
  public void closeResult() throws IOException { }

  @Override
  protected void cls() throws IOException { }

  @Override
  public void comment(final byte[] b) throws IOException { }

  @Override
  protected void empty() throws IOException { }

  @Override
  protected void finish() throws IOException { }

  @Override
  public void item(final byte[] b) throws IOException { }

  @Override
  public void openResult() throws IOException { }

  @Override
  public void pi(final byte[] n, final byte[] v) throws IOException { }

  @Override
  protected void start(final byte[] t) throws IOException { }

  @Override
  public void text(final byte[] b) throws IOException { }

  @Override
  public void text(final byte[] b, final FTPos ftp) throws IOException { }

}
