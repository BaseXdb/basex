(:~
 : Database main page.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/databases';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

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
  %rest:path("/dba/database")
  %rest:query-param("name",     "{$name}", "")
  %rest:query-param("resource", "{$resource}")
  %rest:query-param("sort",     "{$sort}", "")
  %rest:query-param("page",     "{$page}", 1)
  %rest:query-param("info",     "{$info}")
  %rest:query-param("error",    "{$error}")
  %output:method("html")
function dba:database(
  $name      as xs:string,
  $resource  as xs:string?,
  $sort      as xs:string,
  $page      as xs:integer,
  $info      as xs:string?,
  $error     as xs:string?
) as element() {
  cons:check(),
  if(not($name)) then web:redirect("databases") else

  let $db-exists := db:exists($name)
  return html:wrap(
    map {
      'header': ($dba:CAT, $name), 'info': $info, 'error': $error,
      'css': 'codemirror/lib/codemirror.css',
      'scripts': ('codemirror/lib/codemirror.js', 'codemirror/mode/xml/xml.js')
    },
    <tr>
      <td width='49%'>
        <form action="{ $dba:SUB }" method="post" id="{ $dba:SUB }" class="update">
          <input type="hidden" name="name" value="{ $name }" id="name"/>
          <h2>{
            html:link('Databases', $dba:CAT), ' » ',
            $name ! (if(empty($resource)) then . else html:link(., $dba:SUB, map { 'name': . } ))
          }</h2>
          {
            if($db-exists) then (
              let $headers := (
                <resource>Name</resource>,
                <type>Content type</type>,
                <raw>Raw</raw>,
                <size type='number' order='desc'>Size</size>
              )
              let $rows :=
                let $start := util:start($page, $sort)
                let $end := util:end($page, $sort)
                for $res in db:list-details($name)[position() = $start to $end]
                return <row resource='{ $res }' type='{ $res/@content-type }'
                            raw='{ if($res/@raw = 'true') then '✓' else '–' }'
                            size='{ $res/@size }'/>
              let $buttons := (
                html:button('db-add', 'Add…'),
                html:button('db-delete', 'Delete', true()),
                html:button('db-copy', 'Copy…', false()),
                html:button('db-alter', 'Rename…', false()),
                html:button('db-optimize', 'Optimize…', false(), 'global')
              )
              let $map := map { 'name': $name }
              let $link := function($value) { $dba:SUB }
              let $count := count(db:list($name))
              return html:table($headers, $rows, $buttons, $map,
                map { 'sort': $sort, 'link': $link, 'page': $page, 'count': $count })
            ) else ()
          }
        </form>
        <form action="{ $dba:SUB }" method="post" class="update">
          <input type="hidden" name="name" value="{ $name }"/>
          <h3>Backups</h3>
          {
            let $headers := (
              <backup order='desc'>Name</backup>,
              <size type='bytes'>Size</size>
            )
            let $rows :=
              for $backup in db:backups($name)
              order by $backup descending
              return <row backup='{ $backup }' size='{ $backup/@size }'/>
            let $buttons := (
              html:button('backup-create', 'Create', false(), 'global') update (
                if($db-exists) then () else insert node attribute disabled { '' } into .
              ),
              html:button('backup-restore', 'Restore', true()),
              html:button('backup-drop', 'Drop', true())
            )
            let $map := map { 'name': $name }
            let $link := function($value) { 'backup/' || $value || '.zip' }
            return html:table($headers, $rows, $buttons, $map, map { 'link': $link })
          }
        </form>
      </td>
      <td class='vertical'/>
      <td width='49%'>{
        if($resource) then (
          <h3>{ $resource }</h3>,
          <form action="resource" method="post" id="resources" enctype="multipart/form-data">
            <input type="hidden" name="name" value="{ $name }"/>
            <input type="hidden" name="resource" value="{ $resource }" id="resource"/>
            { html:button('db-rename', 'Rename…') }
            { html:button('db-download', 'Download') }
            { html:button('db-replace', 'Replace…') }
          </form>,
          <h4>Enter your query…</h4>,
          <input style="width:100%" name="input" id="input" onkeyup='queryResource(false)'/>,
          <div class='small'/>,
          <textarea name='output' id='output' rows='20' readonly='' spellcheck='false'/>,
          html:focus('input'),
          html:js('loadCodeMirror(false); queryResource(true);')
        ) else if($db-exists) then (
          html:properties(db:info($name))
        ) else ()
      }</td>
    </tr>
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
  %rest:path("/dba/database")
  %rest:form-param("action",   "{$action}")
  %rest:form-param("name",     "{$name}")
  %rest:form-param("resource", "{$resources}")
  %rest:form-param("backup",   "{$backups}")
function dba:database-redirect(
  $action     as xs:string,
  $name       as xs:string,
  $resources  as xs:string*,
  $backups    as xs:string*
) as element(rest:response) {
  web:redirect($action, map { 'name': $name, 'resource': $resources, 'backup': $backups })
};
