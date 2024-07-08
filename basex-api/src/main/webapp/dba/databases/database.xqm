(:~
 : Database main page.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/databases';

import module namespace html = 'dba/html' at '../lib/html.xqm';
import module namespace utils = 'dba/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';
(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : Manages a single database.
 : @param  $name      database
 : @param  $resource  resource
 : @param  $sort      table sort key
 : @param  $page      current page
 : @param  $info      info string
 : @param  $error     error string
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/database')
  %rest:query-param('name',     '{$name}', '')
  %rest:query-param('resource', '{$resource}')
  %rest:query-param('sort',     '{$sort}', '')
  %rest:query-param('page',     '{$page}', 1)
  %rest:query-param('info',     '{$info}')
  %rest:query-param('error',    '{$error}')
  %output:method('html')
  %output:html-version('5')
function dba:database(
  $name      as xs:string,
  $resource  as xs:string?,
  $sort      as xs:string,
  $page      as xs:integer,
  $info      as xs:string?,
  $error     as xs:string?
) as element() {
  if(not($name)) then web:redirect('databases') else

  let $db-exists := db:exists($name)
  return html:wrap({ 'header': ($dba:CAT, $name), 'info': $info, 'error': $error },
    <tr>{
      <td>
        <form method='post'>
          <input type='hidden' name='name' value='{ $name }' id='name'/>
          <h2>{
            html:link('Databases', $dba:CAT), ' » ',
            $name ! (if(empty($resource)) then . else html:link(., $dba:SUB, { 'name': . } ))
          }</h2>
          {
            if($db-exists) then (
              let $headers := (
                { 'key': 'resource' , 'label': 'Name' },
                { 'key': 'type' , 'label': 'Type' },
                { 'key': 'binary' , 'label': 'Binary' },
                { 'key': 'size' , 'label': 'Size', 'type': 'number', 'order': 'desc' }
              )
              let $entries :=
                let $start := utils:start($page, $sort)
                let $end := utils:end($page, $sort)
                for $res in db:list-details($name)[position() = $start to $end]
                return {
                  'resource': $res,
                  'type': $res/@type,
                  'binary': if($res/@raw = 'true') then '✓' else '–',
                  'size': $res/@size
                }
              let $buttons := (
                html:button('db-put', 'Put…'),
                html:button('db-delete', 'Delete', ('CHECK', 'CONFIRM')),
                html:button('db-copy', 'Copy…'),
                html:button('db-alter', 'Rename…'),
                html:button('db-optimize', 'Optimize…')
              )
              let $params := { 'name': $name }
              let $options := {
                'sort': $sort,
                'link': $dba:SUB,
                'page': $page,
                'count': count(db:list($name))
              }
              return html:table($headers, $entries, $buttons, $params, $options)
            ) else ()
          }
        </form>
      </td>,
      if(not($resource)) then (
        <td class='vertical'/>,
        <td>
          <form method='post'>
            <input type='hidden' name='name' value='{ $name }'/>
            <h2>Backups</h2>
            {
              let $headers := (
                { 'key': 'backup', 'label': 'Name', 'order': 'desc' },
                { 'key': 'size', 'label': 'Size', 'type': 'bytes' },
                { 'key': 'comment', 'label': 'Comment' },
                { 'key': 'action', 'label': 'Action', 'type': 'dynamic' }
              )
              let $entries :=
                for $backup in db:backups($name)
                order by $backup descending
                return {
                  'backup': substring-after($backup, $name || '-'),
                  'size': $backup/@size,
                  'comment': $backup/@comment,
                  'action': fn() {
                    html:link('Download', 'backup/' || encode-for-uri($backup) || '.zip')
                  }
                }
              let $buttons := (
                html:button('backup-create', 'Create…') update {
                  if($db-exists) then () else insert node attribute disabled { '' } into .
                },
                html:button('backup-restore', 'Restore', ('CHECK', 'CONFIRM')),
                html:button('backup-drop', 'Drop', ('CHECK', 'CONFIRM'))
              )
              let $params := { 'name': $name }
              return html:table($headers, $entries, $buttons, $params)
            }
          </form>
        </td>
      ),
      <td class='vertical'/>,
      <td>{
        if($resource) then (
          <h2>Resource: { $resource }</h2>,
          <form method='post'>
            <input type='hidden' name='name' value='{ $name }'/>
            <input type='hidden' name='resource' value='{ $resource }' id='resource'/>
            {
              html:button('db-rename', 'Rename…'), ' ',
              html:button('db-download', 'Download'), ' ',
              html:button('db-replace', 'Replace…')
            }
          </form>,
          <b>Enter your query…</b>,
          <input type='text' style='width:100%' name='input' id='input' onkeyup='queryResource(false)'
            autofocus='autofocus'/>,
          <div class='small'/>,
          <textarea name='output' id='output' readonly='' spellcheck='false'/>,
          html:js('loadCodeMirror("xml", false, true); queryResource(true);')
        ) else if($db-exists) then (
          <h2>Information</h2>,
          html:properties(db:info($name))
        ) else ()
      }</td>
    }</tr>
  )
};
