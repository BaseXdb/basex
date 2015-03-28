(:~
 : Queries page.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace _ = 'dba/queries';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../modules/tmpl.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $_:CAT := 'queries';

(:~
 : Queries page.
 : @param  $query  input query
 : @param  $error  error string
 : @param  $info   info string
 : @return HTML page
 :)
declare
  %rest:GET
  %rest:path("dba/queries")
  %rest:query-param("query",  "{$query}")
  %rest:query-param("error",  "{$error}")
  %rest:query-param("info",   "{$info}")
  %output:method("html")
function _:queries(
  $query  as xs:string?,
  $error  as xs:string?,
  $info   as xs:string?
) as element() {
  cons:check(),

  let $f := function($b) { "xquery('Please wait…', 'Query was successful.', " || $b || ");" }
  return tmpl:wrap(map { 'top': $_:CAT, 'info': $info, 'error': $error },
    <tr>
      <td width='50%'>
        <table width='100%'>
          <tr>
            <td>
              <button id='run' onClick="{ $f(true()) }">Run</button>
              <select id='mode' onChange='{ $f(false()) }'>{
                ('Standard', 'Realtime', 'Updating') ! element option { . }
              }</select>
            </td>
            <td align='right'>
              <h2>Editor</h2>
            </td>
          </tr>
        </table>
        <textarea id='editor' rows='20' spellcheck='false' onkeyup="{ $f(false()) }">{
          $query
        }</textarea>
        { html:focus('editor') }
      </td>
      <td width='50%'>
        <table width='100%'>
          <tr>
            <td align='right'>
              <h2>Result</h2>
            </td>
          </tr>
        </table>
        <textarea id='output' rows='20' readonly='' spellcheck='false'/>
      </td>
    </tr>
  )
};

(:~
 : Evaluates a query and returns the result.
 : @param  $query  query
 : @return result of query
 :)
declare
  %rest:path("dba/eval-query")
  %rest:query-param("query", "{$query}")
  %output:method("text")
function _:eval-query(
  $query  as xs:string?
) as xs:string {
  cons:check(),
  util:query($query)
};

(:~
 : Runs an updating query.
 : @param  $query  query
 :)
declare
  %updating
  %rest:path("dba/update-query")
  %rest:query-param("query", "{$query}")
  %output:method("text")
function _:update-query(
  $query  as xs:string?
) {
  cons:check(),
  util:update-query($query)
};

(:~
 : Returns the options for evaluating a query.
 : @return options
 :)
declare %private function _:query-options() {
  "map { 'timeout':" || $cons:TIMEOUT ||
       ",'memory':" || $cons:MEMORY ||
       ",'permission':'" || $cons:PERMISSION || "' }"
};
