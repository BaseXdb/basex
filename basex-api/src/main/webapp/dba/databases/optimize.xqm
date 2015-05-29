(:~
 : Optimize databases.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
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

  return tmpl:wrap(map { 'top': $_:CAT, 'error': $error },
    let $first := not($opts = 'x')
    let $cb := function($option, $label) {
      let $c := if($first)
                then $data//*[name() = $option] = 'true'
                else $opts = $option
      return html:checkbox("opts", $option, $c, $label)
    }
    return <tr>
      <td>
        <form action="optimize" method="post">
          <h2>
            <a href="{ $_:CAT }">Databases</a> »
            { html:link($name, 'database', map { 'name': $name }) } »
            { html:button('optimize', 'Optimize') }
          </h2>
          <!-- dummy value; prevents reset of options when nothing is selected -->
          <input type="hidden" name="name" value="{ $name }"/>
          <input type="hidden" name="opts" value="x"/>
          <table>
            <tr>
              <td colspan="2">
                { html:checkbox("all", 'all', exists($all), 'Full optimization') }
                <h3>{ $cb('textindex', 'Text Index') }</h3>
                <h3>{ $cb('attrindex', 'Attribute Index') }</h3>
                <h3>{ $cb('ftindex', 'Fulltext Index') }</h3>
              </td>
            </tr>
            <tr>
              <td colspan="2">
                { $cb('stemming', 'Stemming') }<br/>
                { $cb('casesens', 'Case Sensitivity') }<br/>
                { $cb('diacritics', 'Diacritics') }<br/>
              </td>
            </tr>
            <tr>
              <td>Language:</td>
              <td><input type="text" name="lang" id="lang" value="{
                if($first) then $data//language else $lang
              }"/></td>
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
  %rest:query-param("name", "{$name}")
  %rest:query-param("all",  "{$all}")
  %rest:query-param("opts", "{$opts}")
  %rest:query-param("lang", "{$lang}")
function _:optimize(
  $name  as xs:string,
  $all   as xs:string?,
  $opts  as xs:string*,
  $lang  as xs:string?
) {
  try {
    cons:check(),
    util:update("db:optimize($name, boolean($all), map:merge((
  (('textindex','attrindex','ftindex','stemming','casesens','diacritics') !
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
