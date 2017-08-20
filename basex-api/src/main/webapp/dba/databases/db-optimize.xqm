(:~
 : Optimize databases.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/databases';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';
(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : Form for optimizing a database.
 : @param  $name   entered name
 : @param  $all    optimize all
 : @param  $opts   database options
 : @param  $lang   language
 : @param  $error  error string
 : @return page
 :)
declare
  %rest:GET
  %rest:path("/dba/db-optimize")
  %rest:query-param("name",  "{$name}")
  %rest:query-param("all",   "{$all}")
  %rest:query-param("opts",  "{$opts}")
  %rest:query-param("lang",  "{$lang}", "en")
  %rest:query-param("error", "{$error}")
  %output:method("html")
function dba:create(
  $name   as xs:string,
  $all    as xs:string?,
  $opts   as xs:string*,
  $lang   as xs:string?,
  $error  as xs:string?
) as element(html) {
  cons:check(),
  let $opts := if($opts = 'x') then $opts else db:info($name)//*[text() = 'true']/name()
  let $lang := if($opts = 'x') then $lang else 'en'
  return html:wrap(map { 'header': ($dba:CAT, $name), 'error': $error },
    <tr>
      <td>
        <form action="db-optimize" method="post">
          <h2>{
            html:link('Databases', $dba:CAT), ' » ',
            html:link($name, 'database', map { 'name': $name }), ' » ',
            html:button('db-optimize', 'Optimize')
          }</h2>
          <!-- dummy value; prevents reset of options if nothing is selected -->
          <input type="hidden" name="opts" value="x"/>
          <input type="hidden" name="name" value="{ $name }"/>
          <table>
            <tr>
              <td colspan="2">
                { html:checkbox("all", 'all', exists($all), 'Full optimization') }
                <h3>{ html:option('textindex', 'Text Index', $opts) }</h3>
                <h3>{ html:option('attrindex', 'Attribute Index', $opts) }</h3>
                <h3>{ html:option('tokenindex', 'Token Index', $opts) }</h3>
                <h3>{ html:option('ftindex', 'Fulltext Index', $opts) }</h3>
              </td>
            </tr>
            <tr>
              <td colspan="2">{
                html:option('stemming', 'Stemming', $opts),
                html:option('casesens', 'Case Sensitivity', $opts),
                html:option('diacritics', 'Diacritics', $opts)
              }</td>
            </tr>
            <tr>
              <td>Language:</td>
              <td><input type="text" name="lang" id="lang" value="{ $lang }"/></td>
              { html:focus('lang') }
            </tr>
          </table>
        </form>
      </td>
    </tr>
  )
};

(:~
 : Optimizes the current database.
 : @param  $name  database
 : @param  $all   optimize all
 : @param  $opts  database options
 : @param  $lang  language
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path("/dba/db-optimize")
  %rest:form-param("name", "{$name}")
  %rest:form-param("all",  "{$all}")
  %rest:form-param("opts", "{$opts}")
  %rest:form-param("lang", "{$lang}")
function dba:db-optimize(
  $name  as xs:string,
  $all   as xs:string?,
  $opts  as xs:string*,
  $lang  as xs:string?
) as empty-sequence() {
  try {
    cons:check(),
    db:optimize($name, boolean($all), map:merge((
      ('textindex','attrindex','tokenindex','ftindex','stemming','casesens','diacritics') !
        map:entry(., $opts = .),
      $lang ! map:entry('language', .)
    ))),
    cons:redirect($dba:SUB, map { 'name': $name, 'info': 'Database was optimized.' })
  } catch * {
    cons:redirect($dba:SUB, map {
      'name': $name, 'opts': $opts, 'lang': $lang, 'error': $err:description
    })
  }
};

(:~
 : Optimizes databases with the current settings.
 : @param  $names  names of databases
 : @return redirection
 :)
declare
  %updating
  %rest:GET
  %rest:path("/dba/db-optimize-all")
  %rest:query-param("name", "{$names}")
  %output:method("html")
function dba:drop(
  $names  as xs:string*
) as empty-sequence() {
  cons:check(),
  try {
    $names ! db:optimize(.),
    cons:redirect($dba:CAT, map { 'info': util:info($names, 'database', 'optimized') })
  } catch * {
    cons:redirect($dba:CAT, map { 'error': $err:description })
  }
};
