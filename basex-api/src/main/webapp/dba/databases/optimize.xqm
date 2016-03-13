(:~
 : Optimize databases.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace _ = 'dba/databases';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../modules/tmpl.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $_:CAT := 'databases';
(:~ Sub category :)
declare variable $_:SUB := 'database';

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
  %rest:path("/dba/optimize")
  %rest:query-param("name",  "{$name}")
  %rest:query-param("all",   "{$all}")
  %rest:query-param("opts",  "{$opts}")
  %rest:query-param("lang",  "{$lang}", "en")
  %rest:query-param("error", "{$error}")
  %output:method("html")
function _:create(
  $name   as xs:string,
  $all    as xs:string?,
  $opts   as xs:string*,
  $lang   as xs:string?,
  $error  as xs:string?
) as element(html) {
  cons:check(),

  let $data := try {
    util:eval('db:info($n)', map { 'n': $name })
  } catch * {
    element error { $cons:DATA-ERROR || ': ' || $err:description }
  }
  let $error := ($data/self::error/string(), $error)[1]
  let $opts := if($opts = 'x') then $opts else $data//*[text() = 'true']/name()
  let $lang := if($opts = 'x') then $lang else 'en'

  return tmpl:wrap(map { 'top': $_:CAT, 'error': $error },
    <tr>
      <td>
        <form action="optimize" method="post">
          <h2>
            <a href="{ $_:CAT }">Databases</a> »
            { html:link($name, 'database', map { 'name': $name }) } »
            { html:button('optimize', 'Optimize') }
          </h2>
          <!-- dummy value; prevents reset of options when nothing is selected -->
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
 :)
declare
  %updating
  %rest:POST
  %rest:path("/dba/optimize")
  %rest:form-param("name", "{$name}")
  %rest:form-param("all",  "{$all}")
  %rest:form-param("opts", "{$opts}")
  %rest:form-param("lang", "{$lang}")
function _:optimize(
  $name  as xs:string,
  $all   as xs:string?,
  $opts  as xs:string*,
  $lang  as xs:string?
) {
  try {
    cons:check(),
    util:update("db:optimize($name, boolean($all), map:merge((
  (('textindex','attrindex','tokenindex','ftindex','stemming','casesens','diacritics') !
    map:entry(., $opts = .)),
    $lang ! map:entry('language', .)
  )
))", map { 'name': $name, 'all': $all, 'lang': $lang, 'opts': $opts }
    ),
    db:output(web:redirect($_:SUB, map {
      'name': $name,
      'info': 'Database was optimized.'
    }))
  } catch * {
    db:output(web:redirect($_:SUB, map {
      'error': $err:description,
      'name': $name,
      'opts': $opts,
      'lang': $lang
    }))
  }
};

(:~
 : Optimizes databases with the current settings.
 : @param  $names  names of databases
 :)
declare
  %updating
  %rest:GET
  %rest:path("/dba/optimize-all")
  %rest:query-param("name", "{$names}")
  %output:method("html")
function _:drop(
  $names  as xs:string*
) {
  cons:check(),
  try {
    util:update("$n ! db:optimize(.)", map { 'n': $names }),
    db:output(web:redirect($_:CAT, map { 'info': 'Optimized databases: ' || count($names) }))
  } catch * {
    db:output(web:redirect($_:CAT, map { 'error': $err:description }))
  }
};
