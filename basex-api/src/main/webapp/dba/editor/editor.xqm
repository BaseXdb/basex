(:~
 : Editor page.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/editor';

import module namespace config = 'dba/config' at '../lib/config.xqm';
import module namespace html = 'dba/html' at '../lib/html.xqm';

(:~ Top category. :)
declare variable $dba:CAT := 'editor';

(:~
 : Editor page.
 : @param  $error  error string
 : @param  $info   info string
 : @param  $file   file to be opened
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/editor')
  %rest:query-param('error', '{$error}')
  %rest:query-param('info',  '{$info}')
  %rest:query-param('file',  '{$file}')
  %output:method('html')
  %output:html-version('5')
function dba:editor(
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
    (
      <tr>
        <td colspan='2'>
          <form autocomplete='off' action='javascript:void(0);'>{
            <datalist id='files'>{ config:files() ! element option { . } }</datalist>,
            intersperse((
              <input type='text' id='file' name='file' placeholder='Name of file'
                     list='files' oninput='checkButtons()' onpropertychange='checkButtons()'/>,
              <button type='submit' name='open' id='open' disabled='' onclick='openFile()'>Open</button>,
              <button name='save' id='save' disabled='' onclick='saveFile()'>Save</button>,
              <button name='close' id='close' disabled='' onclick='closeFile()'>Close</button>,
              <span>  </span>,
              <button id='run' onclick='runQuery()' title='Ctrl-Enter'>Run</button>,
              <button id='stop' onclick='stopQuery(true)' disabled=''>Stop</button>
            ), <span> </span>),
            <h2 class='right'>Result</h2>
          }</form>
        </td>
      </tr>,
      <tr>
        <td width='50%'>
          <textarea id='editor' name='editor'/>
          { html:focus('editor') }
        </td>
        <td width='50%'>{
          <textarea name='output' id='output' readonly=''/>,
          html:js('loadCodeMirror("xquery", true, true);'),
          for $name in head(($file, config:file())[.])
          return html:js('openFile("' || $name || '");')
        }</td>
      </tr>
    )
  )
};
