(:~
 : Editor.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/editor';

import module namespace config = 'dba/config' at '../lib/config.xqm';
import module namespace html = 'dba/html' at '../lib/html.xqm';

(:~ Top category. :)
declare variable $dba:CAT := 'editor';

(:~
 : Editor.
 : @param  $error  error string
 : @param  $info   info string
 : @param  $name   name of edited file
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/editor')
  %rest:query-param('error', '{$error}')
  %rest:query-param('info',  '{$info}')
  %rest:query-param('name',  '{$name}')
  %output:method('html')
  %output:html-version('5')
function dba:editor(
  $error  as xs:string?,
  $info   as xs:string?,
  $name   as xs:string?
) as element(html) {
  (: register file to be edited :)
  let $edited := if ($name) {
    config:set-edited-file(config:files-dir() || $name),
    $name
  } else {
    config:edited-file()
  }
  return (
    <tr>
      <td colspan='2'>
        <form autocomplete='off' action='javascript:void(0);'>{
          <datalist id='files'>{ config:editor-files() ! element option { . } }</datalist>,
          sequence-join((
            <input type='text' id='file' name='file' placeholder='Name of file'
                   list='files' oninput='checkButtons()' onpropertychange='checkButtons()'/>,
            <button type='submit' name='open' id='open' disabled=''
                    onclick='openFile()'>Open</button>,
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
        <textarea id='editor' autofocus='' spellcheck='false'/>
      </td>
      <td width='50%'>{
        <textarea id='output' readonly='' spellcheck='false'/>,
        html:js('loadCodeMirror("xquery", true, true);'),
        $edited ! html:js('openFile("' || file:name(.) || '");')
      }</td>
    </tr>
  ) => html:wrap({ 'header': $dba:CAT, 'info': $info, 'error': $error })
};
