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

(:~ Top category. :)
declare variable $_:CAT := 'queries';
(:~ Query file suffix. :)
declare variable $_:SUFFIX := '.txt';

(:~
 : Queries page.
 : @param  $error   error string
 : @param  $info    info string
 : @return HTML page
 :)
declare
  %rest:GET
  %rest:path("/dba/queries")
  %rest:query-param("error",  "{$error}")
  %rest:query-param("info",   "{$info}")
  %output:method("html")
function _:queries(
  $error   as xs:string?,
  $info    as xs:string?
) as element() {
  cons:check(),

  let $f := function($b) { "editor('Please wait…', 'Query was successful.', " || $b || ");" }
  return tmpl:wrap(map { 'top': $_:CAT, 'info': $info, 'error': $error },
    <tr>
      <td width='50%'>
        <table width='100%'>
          <tr>
            <td width='80%'>
              <select id='mode' onchange='{ $f(false()) }'>{
                ('Standard', 'Realtime', 'Updating') ! element option { . }
              }</select>
              <button id='run' onclick="{ $f(true()) }">Run</button>
            </td>
            <td width='20%' align='right'>
              <h2>Editor</h2>
            </td>
          </tr>
        </table>
        <textarea id='editor' name='editor' rows='20' spellcheck='false' onkeyup="{ $f(false()) }"/>
        <table width='100%'>
          <tr>
            <td>
              <div align='right'>
                <input id='file' name='file' list='files' autocomplete='off' placeholder='Name of query'
                       oninput='checkButtons()' onpropertychange='checkButtons()'/>
                <datalist id='files'>{
                  file:list($cons:QUERY-DIR) ! element option { replace(., $_:SUFFIX || '$', '') }
                }</datalist>
                <button type='submit' name='open' id='open' disabled='true'
                        onclick='openQuery()'>Open</button>
                <button type='save' name='save' id='save' disabled='true'
                        onclick='saveQuery()'>Save</button>
                <button type='delete' name='delete' id='delete' disabled='true'
                        onclick='deleteQuery()'>Delete</button>
                <script type="text/javascript" src="files/editor.js"/>
                <script type="text/javascript" src="files/codemirror/lib/codemirror.js"/>
                <script type="text/javascript" src="files/codemirror/mode/xquery/xquery.js"/>
                <script type="text/javascript" src="files/codemirror/mode/xml/xml.js"/>
                <script type="text/javascript">loadCodeMirrorCSS();</script>
              </div>
            </td>
          </tr>
        </table>
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
        <script type="text/javascript">loadCodeMirror();</script>
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
  %rest:path("/dba/eval-query")
  %rest:query-param("query", "{$query}")
  %output:method("text")
function _:eval-query(
  $query  as xs:string?
) as xs:string {
  cons:check(),
  util:query($query)
};

(:~
 : Returns the contents of a query file.
 : @param  $name  name of query file (without suffix)
 : @return query string
 :)
declare
  %rest:path("/dba/open-query")
  %rest:query-param("name", "{$name}")
  %output:method("text")
function _:open-query(
  $name  as xs:string
) as xs:string {
  cons:check(),
  file:read-text(_:to-path($name))
};

(:~
 : Delete a query file and returns the names of stored queries.
 : @param  $name  name of query file (without suffix)
 : @return names of stored queries
 :)
declare
  %rest:path("/dba/delete-query")
  %rest:query-param("name", "{$name}")
  %output:method("text")
function _:delete-query(
  $name  as xs:string
) as xs:string {
  cons:check(),
  file:delete(_:to-path($name)),
  string-join(file:list($cons:QUERY-DIR) ! replace(., $_:SUFFIX || '$', ''), '/')
};

(:~
 : Saves a query file and returns the list of stored queries.
 : @param  $name   name of query file (without suffix)
 : @param  $query  query string
 : @return names of stored queries
 :)
declare
  %rest:POST("{$query}")
  %rest:path("/dba/save-query")
  %rest:query-param("name", "{$name}")
  %output:method("text")
function _:save-query(
  $name   as xs:string,
  $query  as xs:string
) as xs:string {
  cons:check(),
  file:write-text(_:to-path($name), $query),
  string-join(file:list($cons:QUERY-DIR) ! replace(., $_:SUFFIX || '$', ''), '/')
};

(:~
 : Runs an updating query.
 : @param  $query  query
 :)
declare
  %updating
  %rest:path("/dba/update-query")
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
declare %private function _:query-options() as xs:string {
  "map { 'timeout':" || $cons:TIMEOUT ||
       ",'memory':" || $cons:MEMORY ||
       ",'permission':'" || $cons:PERMISSION || "' }"
};

(:~
 : Returns a normalized file path for the specified file name.
 : @param  $name  file name
 : @return file path
 :)
declare %private function _:to-path(
  $name  as xs:string
) as xs:string {
  $cons:QUERY-DIR || translate($name, '\/:*?"<>|', '---------') || $_:SUFFIX
};
