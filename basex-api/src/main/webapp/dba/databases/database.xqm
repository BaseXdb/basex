(:~
 : Database main page.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace _ = 'dba/databases';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../modules/tmpl.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $_:CAT := 'databases';
(:~ Sub category :)
declare variable $_:SUB := 'database';

(:~
 : Manages a single database.
 : @param  $name      database
 : @param  $resource  resource
 : @param  $error     error string
 : @param  $info      info string
 : @return page
 :)
declare
  %rest:GET
  %rest:path("dba/database")
  %rest:query-param("name",     "{$name}")
  %rest:query-param("resource", "{$resource}")
  %rest:query-param("error",    "{$error}")
  %rest:query-param("info",     "{$info}")
  %output:method("html")
function _:database(
  $name      as xs:string,
  $resource  as xs:string?,
  $error     as xs:string?,
  $info      as xs:string?
) as element(html) {
  cons:check(),

  (: request data in a single step :)
  let $data := try {
    util:eval('element result {
      let $found := db:exists($name)
      return (
        element found { $found },
        if($found) then (
          element databases { db:list-details($name)[position() = 1 to $max] },
          element info { db:info($name) }
        ) else (),
        element backups { db:backups($name) }
      )
    }', map { 'name': $name, 'max': $cons:MAX-ROWS + 1 })
  } catch * {
    element error { $cons:DATA-ERROR || ': ' || $err:description }
  }
  let $found := $data/found = 'true'
  let $error := ($data/self::error/string(), $error)[1]

  return tmpl:wrap(map { 'top': $_:CAT, 'info': $info, 'error': $error },
    <tr>
      <td width='49%'>
        <form action="{ $_:SUB }" method="post" id="{ $_:SUB }" class="update">
          <input type="hidden" name="name" value="{ $name }" id="name"/>
          <h2><a href="{ $_:CAT }">Databases</a> » {
            $name ! (if(empty($resource)) then . else html:link(., $_:SUB, map { 'name': $name } ))
          }</h2>
          {
            if(not($found)) then () else (
              let $entries := $data/databases/* !
                <e resource='{ . }' ct='{ @content-type }' raw='{
                  if(@raw = 'true') then '✓' else '–'
                }'/>
              let $headers := (
                <resource>{ html:label($entries, ('Resource', 'Resources')) }</resource>,
                <ct>Content type</ct>,
                <raw>Raw</raw>
              )
              let $buttons := (
                html:button('add', 'Add…'),
                html:button('delete', 'Delete', true()),
                html:button('copy', 'Copy…', false()),
                html:button('alter-db', 'Rename…', false()),
                html:button('optimize', 'Optimize…', false(), 'global')
              )
              let $map := map { 'name': $name }
              let $link := function($value) { $_:SUB }
              return html:table($entries, $headers, $buttons, $map, (), $link)
            )
          }
        </form>
        <form action="{ $_:SUB }" method="post" class="update">
          <input type="hidden" name="name" value="{ $name }"/>
          <h3>Backups</h3>
          {
            let $entries :=
              for $backup in $data/backups/*
              order by $backup descending
              return <e backup='{ $backup }' size='{ $backup/@size }'/>
            let $headers := (
              <backup order='desc'>{ html:label($entries, ('Backup', 'Backups')) }</backup>,
              <size type='bytes'>Size</size>
            )
            let $buttons := (
              html:button('create-backup', 'Create', false(), 'global') update (
                if($found) then () else insert node attribute disabled { '' } into .
              ),
              html:button('restore', 'Restore', true()),
              html:button('drop-backup', 'Drop', true())
            )
            let $map := map { 'name': $name }
            let $link := function($value) { 'backup/' || $value || '.zip' }
            return html:table($entries, $headers, $buttons, $map, (), $link)
          }
        </form>
      </td>
      <td class='vertical'/>
      <td width='49%'>{
        if($resource) then <_>
          <h3>{ $resource }</h3>
          <form action="resource" method="post" id="resources" enctype="multipart/form-data">
            <input type="hidden" name="name" value="{ $name }"/>
            <input type="hidden" name="resource" value="{ $resource }" id="resource"/>
            { html:button('rename', 'Rename…') }
            { html:button('download', 'Download') }
            { html:button('replace', 'Replace…') }
          </form>
          <b>XQuery:</b>
          <input style="width:100%" name="input" id="input"
            onkeyup='queryResource("Please wait…", "Query was successful.")'/>
          { html:focus('input') }
          <textarea name='output' id='output' rows='20' readonly='' spellcheck='false'/>
          <script type="text/javascript">queryResource('', '');</script>
        </_>/node() else (
          $data/info/*/html:properties(.)
        )
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
 :)
declare
  %rest:POST
  %rest:path("dba/database")
  %rest:form-param("action",   "{$action}")
  %rest:form-param("name",     "{$name}")
  %rest:form-param("resource", "{$resources}")
  %rest:form-param("backup",   "{$backups}")
function _:action(
  $action     as xs:string,
  $name       as xs:string,
  $resources  as xs:string*,
  $backups    as xs:string*
) {
  web:redirect(
    web:create-url($action, map { 'name': $name, 'resource': $resources, 'backup': $backups })
  )
};
