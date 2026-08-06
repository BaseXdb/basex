package org.basex.util.http;

import static org.basex.util.http.HTTPText.*;
import static org.basex.util.http.RequestAttribute.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.*;
import java.net.http.HttpResponse.*;
import java.time.*;
import java.util.Map.*;

import org.basex.core.StaticOptions.*;
import org.basex.core.jobs.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Single HTTP exchange, which can be repeated.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Exchange {
  /** Target URI. */
  private final URI uri;
  /** Request data. */
  private final Request request;
  /** HTTP client. */
  private final HttpClient client;

  /**
   * Constructor.
   * @param uri target URI
   * @param request request data
   * @param client HTTP client
   */
  public Exchange(final URI uri, final Request request, final HttpClient client) {
    this.uri = uri;
    this.request = request;
    this.client = client;
  }

  /**
   * Returns the target URI.
   * @return URI
   */
  public URI uri() {
    return uri;
  }

  /**
   * Returns the request method.
   * @return method (can be {@code null})
   */
  public String method() {
    return request.attribute(METHOD);
  }

  /**
   * Sends the request and returns the response.
   * @return HTTP response
   * @throws IOException I/O Exception
   */
  public HttpResponse<InputStream> send() throws IOException {
    final HttpRequest.Builder rb;
    try {
      rb = HttpRequest.newBuilder(uri);

      // set timeout
      final String timeout = request.attribute(TIMEOUT);
      if(timeout != null) rb.timeout(Duration.ofSeconds(Strings.toInt(timeout)));

      // set method, attach payload
      final String method = request.attribute(METHOD);
      final String src = request.isMultipart ? null : request.payloadAtts.get(SRC);
      final boolean hasBody = src != null ||
          !(request.payload.isEmpty() && request.parts.isEmpty());
      if(method != null) {
        if(hasBody) setContentType(rb);
        rb.method(method, hasBody ? publisher(src) : HttpRequest.BodyPublishers.noBody());
      }

      // assign headers to request; the Content-Type of a payload request is already set above,
      // so skip it here to avoid sending it twice; ensure that Accept header is sent
      request.headers.forEach((name, value) -> {
        if(!(hasBody && name.equalsIgnoreCase(CONTENT_TYPE))) rb.header(name, value);
      });
      if(Checks.all(request.headers.keySet(), name -> !name.equalsIgnoreCase(ACCEPT))) {
        rb.header(ACCEPT, MediaType.ALL_ALL.toString());
      }
    } catch(final IllegalArgumentException ex) {
      throw new IOException(ex.getMessage(), ex);
    }

    final BodyHandler<InputStream> handler = HttpResponse.BodyHandlers.ofInputStream();

    // send request (with optional authorization)
    try {
      final UserInfo ui = new UserInfo(uri, request);
      final boolean sa = Strings.isTrue(request.attribute(SEND_AUTHORIZATION));
      if(sa && request.authMethod == AuthMethod.BASIC) {
        ui.basic(rb);
      } else {
        final HttpResponse<InputStream> response = Job.run(() -> client.send(rb.build(), handler));
        if(!ui.assign(rb, response)) return response;
      }
      return Job.run(() -> client.send(rb.build(), handler));
    } catch(final InterruptedException | IllegalArgumentException ex) {
      // illegal argument exception may be caused by wrongly encoded redirect URL
      throw new IOException(ex.getMessage(), ex);
    }
  }

  /**
   * Returns a publisher for the request payload. The contents of file-based sources are streamed;
   * other payloads are materialized in advance. Live HTTP response streams are not attached
   * directly, as reading them while sending can deadlock the shared HTTP client.
   * @param src linked resource (can be {@code null})
   * @return publisher
   * @throws IOException I/O exception
   */
  private BodyPublisher publisher(final String src) throws IOException {
    IO io = null;
    if(src != null) {
      io = IO.get(src);
    } else if(request.payload.size() == 1 &&
        request.payload.get(0) instanceof final B64IOLazy bin && !bin.isCached() &&
        Checks.all(request.payloadAtts.entrySet(), att ->
          att.getKey().equals(SerializerOptions.MEDIA_TYPE.name()) &&
          Payload.binary(new MediaType(att.getValue())))) {
      io = bin.input();
    }
    return io instanceof final IOFile file ?
      HttpRequest.BodyPublishers.ofFile(file.file().toPath()) :
      HttpRequest.BodyPublishers.ofByteArray(Client.payload(request));
  }

  /**
   * Sets the content type of the HTTP request.
   * @param rb HTTP request builder
   */
  private void setContentType(final HttpRequest.Builder rb) {
    // look up an explicit Content-Type header (case-insensitively)
    String ct = null;
    for(final Entry<String, String> header : request.headers.entrySet()) {
      if(header.getKey().equalsIgnoreCase(CONTENT_TYPE)) {
        ct = header.getValue();
        break;
      }
    }
    if(ct == null) {
      // no header: @media-type of <http:body/> is considered
      ct = request.payloadAtts.get(SerializerOptions.MEDIA_TYPE.name());
      if(request.isMultipart) ct = Strings.concat(ct, "; ", BOUNDARY, "=", request.boundary());
    } else if(request.isMultipart && new MediaType(ct).parameter(BOUNDARY) == null) {
      // multipart header without boundary: append the generated boundary
      ct = Strings.concat(ct, "; ", BOUNDARY, "=", request.boundary());
    }
    rb.header(CONTENT_TYPE, ct);
  }
}
