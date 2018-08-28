(:~
 : This module contains some basic examples for RESTXQ annotations.
 : @author BaseX Team
 :)
module namespace page = 'http://basex.org/modules/web-page';

(:~
 : Generates a welcome page.
 : @return HTML page
 :)
declare
  %rest:path("")
  %output:method("xhtml")
  %output:omit-xml-declaration("no")
  %output:doctype-public("-//W3C//DTD XHTML 1.0 Transitional//EN")
  %output:doctype-system("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd")
function page:start(
) as element(Q{http://www.w3.org/1999/xhtml}html) {
  <html xmlns="http://www.w3.org/1999/xhtml">
    <head>
      <title>BaseX HTTP Services</title>
      <link rel="stylesheet" type="text/css" href="static/style.css"/>
    </head>
    <body>
      <div class="right"><img src="static/basex.svg" width="96"/></div>
      <h1>BaseX HTTP Services</h1>
      <div>Welcome to the BaseX HTTP Services. They allow you to:</div>
      <ul>
        <li>create web applications and services with
          <a href="http://docs.basex.org/wiki/RESTXQ">RESTXQ</a>,</li>
        <li>use full-duplex communication with
          <a href="http://docs.basex.org/wiki/WebSocket">WebSockets</a>,</li>
        <li>query and modify databases via <a href="http://docs.basex.org/wiki/REST">REST</a>
          (try <a href='rest'>here</a>) and,</li>
        <li>browse and update resources via
          <a href="http://docs.basex.org/wiki/WebDAV">WebDAV</a>, and</li>
      </ul>

      <p>Find more information on the
        <a href="http://docs.basex.org/wiki/Web_Application">Web Application</a>
        page in our documentation.</p>

      <h2>Sample Applications</h2>

      <h3>Database Administration</h3>

      <p>The <a href="dba">DBA</a> is a database administration interface
        written in RESTXQ. The DBA code serves as a good introduction on
        how to build XQuery web applications.</p>

      <h3>WebSocket Chat</h3>

      <p>The <a href="chat">Chat</a> illustrates how bidirectional communication
        can be realized in XQuery.</p>
    </body>
  </html>
};
