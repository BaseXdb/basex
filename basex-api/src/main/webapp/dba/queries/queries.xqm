(:~
 : Queries page.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/queries';

import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace session = 'dba/session' at '../modules/session.xqm';

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
                for $mode in ('Read-Only', 'Updating')
                return element option { $mode }
              }</select>{ ' ' }
              <button id='run' onclick='runQuery()' title='Ctrl-Enter'>Run</button>{ ' ' }
              <button id='stop' onclick='stopQuery()' disabled=''>Stop</button>
            </td>
            <td width='20%' align='right'>
              <h2>Editor</h2>
            </td>
          </tr>
        </table>
        <textarea id='editor' name='editor'/>
        <table width='100%'>
          <form autocomplete='off' action='javascript:void(0);'>
            <tr>
              <td class='slick'>
                <div align='right'>
                  <input id='file' name='file' placeholder='Name of query' size='35'
                         list='files' oninput='checkButtons()' onpropertychange='checkButtons()'/>
                  <datalist id='files'>{
                    for $file in session:query-files()
                    return element option { $file }
                  }</datalist>{ ' ' }
                  <button type='submit' name='open' id='open' disabled=''
                          onclick='openQuery()'>Open</button>{ ' ' }
                  <button name='save' id='save' disabled=''
                          onclick='saveQuery()'>Save</button>{ ' ' }
                  <button name='close' id='close' disabled=''
                          onclick='closeQuery()'>Close</button>
                </div>
              </td>
            </tr>
          </form>
        </table>
        { html:focus('editor') }
      </td>
      <td width='50%'>{
        <table width='100%'>
          <tr>
            <td align='right'>
              <h2>Result</h2>
            </td>
          </tr>
        </table>,
        <textarea name='output' id='output' readonly=''/>,
        html:js('loadCodeMirror(true);'),
        for $name in (($file, session:get($session:QUERY))[.])[1]
        return html:js('openQuery("' || $name || '");')
      }</td>
    </tr>
  )
};
