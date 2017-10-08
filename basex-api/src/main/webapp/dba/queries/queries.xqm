(:~
 : Queries page.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/queries';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';

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
              }</select>{ ' ' }
              <button id='run' onclick='runQuery()' title='Ctrl-Enter'>Run</button>{ ' ' }
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
                  <input id='file' name='file' placeholder='Name of query' size='35'
                         list='files' oninput='checkButtons()' onpropertychange='checkButtons()'/>
                  <datalist id='files'>{ cons:query-files() ! element option { . } }</datalist>{ ' ' }
                  <button type='submit' name='open' id='open' disabled='true'
                          onclick='openQuery()'>Open</button>{ ' ' }
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
          for $name in (($file, $cons:OPTION($cons:K-QUERY))[.])[1]
          return html:js('openQuery("' || $name || '");')
        }
      </td>
    </tr>
  )
};
