(:~
 : Queries page.
 :
 : @author Christian Grün, BaseX Team 2005-23, BSD License
 :)
module namespace dba = 'dba/queries';

import module namespace config = 'dba/config' at '../lib/config.xqm';
import module namespace html = 'dba/html' at '../lib/html.xqm';

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
  %rest:path('/dba/queries')
  %rest:query-param('error', '{$error}')
  %rest:query-param('info',  '{$info}')
  %rest:query-param('file',  '{$file}')
  %output:method('html')
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
                  'codemirror/mode/xquery/xquery.js', 'codemirror/mode/xml/xml.js')
    },
    <tr>
      <td width='50%'>
        <select id='mode' style='width:revert'>{
          for $mode in ('Read-Only', 'Updating')
          return element option { $mode }
        }</select>{ ' ' }
        <button id='run' onclick='runQuery()' title='Ctrl-Enter'>Run</button>{ ' ' }
        <button id='stop' onclick='stopQuery()' disabled=''>Stop</button>
        <div class='right'>
        </div>
        <h2 class='right'>Editor</h2>
        <textarea id='editor' name='editor'/>
        <table width='100%'>
          <form autocomplete='off' action='javascript:void(0);'>
            <tr>
              <td class='slick'>
                <div align='right'>
                  <input type='text' id='file' name='file' placeholder='Name of query'
                         list='files' oninput='checkButtons()' onpropertychange='checkButtons()'/>
                  <datalist id='files'>{
                    for $file in config:query-files()
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
        <h2 class='right'>Result</h2>,
        <textarea name='output' id='output' readonly=''/>,
        html:js('loadCodeMirror("xquery", true, true);'),
        for $name in head(($file, config:query())[.])
        return html:js('openQuery("' || $name || '");')
      }</td>
    </tr>
  )
};
