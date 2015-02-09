(:~
 : Template functions.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace tmpl = 'dba/tmpl';

import module namespace G = 'dba/global' at 'global.xqm';
import module namespace web = 'dba/web' at 'web.xqm';

(:~
 : Extends the specified table rows with the page template.
 : @param  $tr  tr elements
 : @return HTML page
 :)
declare function tmpl:wrap(
  $tr  as element(tr)+
) as element(html) {
  tmpl:wrap(map{ }, $tr)
};

(:~
 : Extends the specified table rows with the page template.
 : The following options can be specified:
 : <ul>
 :   <li><b>top</b>: current top category</li>
 :   <li><b>error</b>: error string</li>
 :   <li><b>info</b>: info string</li>
 : </ul>
 : @param  $options  options
 : @param  $tr       tr elements
 : @return page
 :)
declare function tmpl:wrap(
  $options  as map(*),
  $tr       as element(tr)+
) as element(html) {
  <html>
    <head>
      <meta charset="utf-8"/>
      <title>Database Administration</title>
      <meta name="description" content="Database Administration"/>
      <meta name="author" content="BaseX Team, 2014-15"/>
      <link rel="stylesheet" type="text/css" href="files/style.css"/> 
      <script type="text/javascript" src="files/js.js"/>
    </head>
    <body>
      <div class="right"><img style='padding-left:10px;padding-bottom:10px;' src="files/basex.svg"/></div>
      <h1>Database Administration</h1>
      <div>{
        let $emph := <span>{(element b {
          attribute id { 'info' },
          $options('error')[.] ! (attribute class { 'error' }, .),
          $options('info')[.] ! (attribute class { 'info' }, .),
          ' '
        })}</span>
        return try {
          web:check(),
          let $cats := 
            let $top := $options('top')
            for $cat in ('Databases', 'Queries', 'Logs', 'Users', 'Settings', 'Logout')
            let $link := <a href="{ lower-case($cat) }">{ $cat }</a>
            return if($top = $link) then (
              <b>{ $link }</b>
            ) else (
              $link
            )
          return (head($cats), tail($cats) ! (' | ', .)),
          (1 to 4) ! '&#x2000;',
          $emph
        } catch basex:login {
          $emph
        },
        <span style='float:right'>User: <b>{
          $G:SESSION/name/text()
        }</b></span>[$G:SESSION]
      }</div>
      <hr/>
      <div class='small'/>
      <table width='100%'>{ $tr }</table>
      <hr/>
      <div class='right'><sup>BaseX Team, 2014-15</sup></div>
      <div class='small'/>
      <script type="text/javascript">(function(){{ buttons(); }})();</script>
    </body>
  </html>
};
