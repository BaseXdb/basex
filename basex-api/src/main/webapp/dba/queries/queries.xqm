(:~
 : Queries page.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/queries';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category. :)
declare variable $dba:CAT := 'queries';

(:~
 : Queries page.
 : @param  $error  error string
 : @param  $info   info string
 : @param  $file   file to be opened
 : @return page
 :)
declare
  %rest:GET
  %rest:path("/dba/queries")
  %rest:query-param("error", "{$error}")
  %rest:query-param("info",  "{$info}")
  %rest:query-param("file",  "{$file}")
  %output:method("html")
function dba:queries(
  $error  as xs:string?,
  $info   as xs:string?,
  $file   as xs:string?
) as element(html) {
  cons:check(),
  html:wrap(
    map {
      'header': $dba:CAT, 'info': $info, 'error': $error,
      'css': 'codemirror/lib/codemirror.css',
      'scripts': ('editor.js', 'codemirror/lib/codemirror.js',
                  'codemirror/mode/xquery/xquery.js','codemirror/mode/xml/xml.js')
    },
    <tr>
      <td width='50%'>
        <table width='100%'>
          <tr>
            <td width='80%'>
              <select id='mode'>{
                ('Read-Only', 'Updating') ! element option { . }
              }</select>
              <button id='run' onclick='runQuery()' title='Ctrl-Enter'>Run</button>
              <button id='stop' onclick='stopQuery()' disabled='true'>Stop</button>
            </td>
            <td width='20%' align='right'>
              <h2>Editor</h2>
            </td>
          </tr>
        </table>
        <textarea id='editor' name='editor' rows='20' spellcheck='false'/>
        <table width='100%'>
          <form autocomplete='off' action='javascript:void(0);'>
            <tr>
              <td style='padding-right:0;'>
                <div align='right'>
                    <input id='file' name='file' placeholder='Name of query' size='30'
                           list='files' oninput='checkButtons()' onpropertychange='checkButtons()'/>
                    <datalist id='files'>{ dba:files() ! element option { . } }</datalist>
                    <button type='submit' name='open' id='open' disabled='true'
                            onclick='openQuery()'>Open</button>
                    <button type='save' name='save' id='save' disabled='true'
                            onclick='saveQuery()'>Save</button>
                </div>
              </td>
            </tr>
          </form>
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
        <textarea name='output' id='output' rows='20' readonly='' spellcheck='false'/>
        {
          html:js('loadCodeMirror(true);'),
          if($file) then html:js('openQuery("' || replace($file, '"', '') || '");') else ()
        }
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
  %rest:POST("{$query}")
  %rest:path("/dba/eval-query")
  %rest:single
  %output:method("text")
function dba:eval-query(
  $query  as xs:string?
) as xs:string {
  cons:check(),
  util:query($query, ())
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
function dba:open-query(
  $name  as xs:string
) as xs:string {
  cons:check(),
  file:read-text(dba:to-path($name))
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
function dba:delete-query(
  $name  as xs:string
) as xs:string {
  cons:check(),
  file:delete(dba:to-path($name)),
  string-join(dba:files(), '/')
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
function dba:save-query(
  $name   as xs:string,
  $query  as xs:string
) as xs:string {
  cons:check(),
  file:write-text(dba:to-path($name), $query),
  string-join(dba:files(), '/')
};

(:~
 : Runs an updating query.
 : @param  $query  query
 :)
declare
  %updating
  %rest:POST("{$query}")
  %rest:path("/dba/update-query")
  %rest:single
  %output:method("text")
function dba:update-query(
  $query  as xs:string?
) as empty-sequence() {
  cons:check(),
  util:update-query($query)
};

(:~
 : Returns the options for evaluating a query.
 : @return options
 :)
declare %private function dba:query-options() as xs:string {
  "map { 'timeout':" || $cons:OPTION($cons:K-TIMEOUT) ||
       ",'memory':" || $cons:OPTION($cons:K-MEMORY) ||
       ",'permission':'" || $cons:OPTION($cons:K-PERMISSION) || "' }"
};

(:~
 : Returns a normalized file path for the specified file name.
 : @param  $name  file name
 : @return file path
 :)
declare %private function dba:to-path(
  $name  as xs:string
) as xs:string {
  $cons:DBA-DIR || translate($name, '\/:*?"<>|', '---------') || $cons:SUFFIX
};

(:~
 : Returns the names of all files.
 : @return list of files
 :)
declare %private function dba:files() as xs:string* {
  let $dir := $cons:DBA-DIR
  where file:exists($dir)
  for $file in file:list($dir, false(), '*' || $cons:SUFFIX)
  return replace($file, $cons:SUFFIX || '$', '')
};
