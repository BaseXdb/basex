(:~
 : Database main page.
 :
 : @author Christian Grün, BaseX Team 2005-23, BSD License
 :)
module namespace dba = 'dba/databases';

import module namespace html = 'dba/html' at '../lib/html.xqm';
import module namespace util = 'dba/util' at '../lib/util.xqm';

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
  return html:wrap(
    map {
      'header': ($dba:CAT, $name), 'info': $info, 'error': $error,
      'css': 'codemirror/lib/codemirror.css',
      'scripts': ('codemirror/lib/codemirror.js', 'codemirror/mode/xml/xml.js')
    },
    <tr>{
      <td>
        <form action='{ $dba:SUB }' method='post' id='{ $dba:SUB }' class='update'>
          <input type='hidden' name='name' value='{ $name }' id='name'/>
          <h2>{
            html:link('Databases', $dba:CAT), ' » ',
            $name ! (if(empty($resource)) then . else html:link(., $dba:SUB, map { 'name': . } ))
          }</h2>
          {
            if($db-exists) then (
              let $headers := (
                map { 'key': 'resource' , 'label': 'Name' },
                map { 'key': 'type' , 'label': 'Type' },
                map { 'key': 'binary' , 'label': 'Binary' },
                map { 'key': 'size' , 'label': 'Size', 'type': 'number', 'order': 'desc' }
              )
              let $entries :=
                let $start := util:start($page, $sort)
                let $end := util:end($page, $sort)
                for $res in db:list-details($name)[position() = $start to $end]
                return map {
                  'resource': $res,
                  'type': $res/@type,
                  'binary': if($res/@raw = 'true') then '✓' else '–',
                  'size': $res/@size
                }
              let $buttons := (
                html:button('db-put', 'Put…'),
                html:button('db-delete', 'Delete', true()),
                html:button('db-copy', 'Copy…', false()),
                html:button('db-alter', 'Rename…', false()),
                html:button('db-optimize', 'Optimize…', false(), map { 'class': 'global' })
              )
              let $params := map { 'name': $name }
              let $options := map {
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
          <form action='{ $dba:SUB }' method='post' class='update'>
            <input type='hidden' name='name' value='{ $name }'/>
            <h2>Backups</h2>
            {
              let $headers := (
                map { 'key': 'backup', 'label': 'Name', 'order': 'desc' },
                map { 'key': 'size', 'label': 'Size', 'type': 'bytes' },
                map { 'key': 'comment', 'label': 'Comment' },
                map { 'key': 'action', 'label': 'Action', 'type': 'dynamic' }
              )
              let $entries :=
                for $backup in db:backups($name)
                order by $backup descending
                return map {
                  'backup': substring-after($backup, $name || '-'),
                  'size': $backup/@size,
                  'comment': $backup/@comment,
                  'action': function() {
                    html:link('Download', 'backup/' || encode-for-uri($backup) || '.zip')
                  }
                }
              let $buttons := (
                html:button('backup-create', 'Create…', false(), map { 'class': 'global' }) update {
                  if($db-exists) then () else insert node attribute disabled { '' } into .
                },
                html:button('backup-restore', 'Restore', true()),
                html:button('backup-drop', 'Drop', true())
              )
              let $params := map { 'name': $name }
              return html:table($headers, $entries, $buttons, $params, map { })
            }
          </form>
        </td>
      ),
      <td class='vertical'/>,
      <td>{
        if($resource) then (
          <h2>Resource: { $resource }</h2>,
          <form action='resource' method='post' id='resources'>
            <input type='hidden' name='name' value='{ $name }'/>
            <input type='hidden' name='resource' value='{ $resource }' id='resource'/>
            {
              html:button('db-rename', 'Rename…'), ' ',
              html:button('db-download', 'Download'), ' ',
              html:button('db-replace', 'Replace…')
            }
          </form>,
          <b>Enter your query…</b>,
          <input type='text' style='width:100%' name='input' id='input'
                 onkeyup='queryResource(false)'/>,
          <div class='small'/>,
          <textarea name='output' id='output' readonly='' spellcheck='false'/>,
          html:focus('input'),
          html:js('loadCodeMirror("xml", false, true); queryResource(true);')
        ) else if($db-exists) then (
          <h2>Information</h2>,
          html:properties(db:info($name))
        ) else ()
      }</td>
    }</tr>
  )
};

(:~
 : Redirects to the specified action.
 : @param  $action     action to perform
 : @param  $name       database
 : @param  $resources  resources
 : @param  $backups    backups
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path('/dba/database')
  %rest:form-param('action',   '{$action}')
  %rest:form-param('name',     '{$name}')
  %rest:form-param('resource', '{$resources}')
  %rest:form-param('backup',   '{$backups}')
function dba:database-redirect(
  $action     as xs:string,
  $name       as xs:string,
  $resources  as xs:string*,
  $backups    as xs:string*
) as element(rest:response) {
  web:redirect($action, map { 'name': $name, 'resource': $resources, 'backup': $backups })
};
