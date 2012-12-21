(:~
 : Direct evalutation of XQuery files.
 : The DirectServlet needs to be activated
 : to evaluate this file server-side.
 :)
declare option output:method 'xhtml';
declare option output:omit-xml-declaration 'no';

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title>XQuery Evaluation</title>
    <link rel="stylesheet" type="text/css" href="/style.css"/>
  </head>
  <body>
    <div class="right"><img src="/basex.svg" width="96"/></div>
    <h2>XQuery Evaluation</h2>
    <div>This page was directly created by the addressed XQuery file.<br/>
      Result of <code>1 to 10</code>: { 1 to 10 }.
    </div>
    <hr/>
    <p>The source of this file (<code>{ static-base-uri() }</code>)
    is shown below:</p>
    <pre>{ unparsed-text(static-base-uri()) }</pre>
    <p class='right'><a href='..'>...back to main page</a></p>
  </body>
</html>
