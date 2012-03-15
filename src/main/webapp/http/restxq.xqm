module namespace rest = "http://exquery.org/ns/rest/annotation";

declare
  %rest:path("")
  %output:method("xhtml")
  %output:doctype-public("-//W3C//DTD HTML 4.01//EN")
  %output:doctype-system("http://www.w3.org/TR/html4/strict.dtd")
  function rest:start() {
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <title>BaseX HTTP Server</title>
        <style type="text/css"><!--
        a { text-decoration: none; }
        body { font-family:Ubuntu,'Trebuchet MS',SansSerif; }
        --></style>
      </head>
      <body>
        <h2>Welcome to RESTXQ: RESTful Annotations for XQuery</h2>
        <p>This page is generates in an XQuery module, which is located in the
        web server's root directory (specified by the <code>HTTPPATH</code> option).</p>
        <p>...</p>
      </body>
    </html>
};
