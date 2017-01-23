(:~
 : Database main page.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace dba = 'dba/databases';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../modules/tmpl.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';
(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : Manages a single database.
 : @param  $name      database
 : @param  $resource  resource
 : @param  $error     error string
 : @param  $info      info string
 : @param  $page   current page
 : @return page
 :)
declare
  %rest:GET
  %rest:path("/dba/database")
  %rest:query-param("name",     "{$name}")
  %rest:query-param("resource", "{$resource}")
  %rest:query-param("sort",     "{$sort}", "")
  %rest:query-param("page",     "{$page}", 1)
  %rest:query-param("info",     "{$info}")
  %rest:query-param("error",    "{$error}")
  %output:method("html")
function dba:database(
  $name      as xs:string?,
  $resource  as xs:string?,
  $sort      as xs:string,
  $page      as xs:integer,
  $info      as xs:string?,
  $error     as xs:string?
) as element() {
  cons:check(),
  if(not($name)) then web:redirect("databases") else

  let $data := try {
    let $found := db:exists($name)
    return (
      element found { $found },
      element count { count(db:list($name)) },
      if($found) then (
        db:info($name),
        let $start := util:start($page, $sort)
        let $end := util:end($page, $sort)
        return db:list-details($name)[position() = $start to $end]
      ) else (),
      db:backups($name)
    )
  } catch * {
    element error { $err:description }
  }
  let $error := head(($data/self::error, $error))
  let $only-backups := $data/self::found = 'false'

  return tmpl:wrap(
    map {
      'top': $dba:CAT, 'info': $info, 'error': $error,
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
            if($only-backups) then () else (
              let $count := xs:integer($data/self::count)
              let $rows :=
                for $res in $data/self::resource
                return <row resource='{ $res }' type='{ $res/@content-type }'
                            raw='{ if($res/@raw = 'true') then '&#x2713;' else '–' }'
                            size='{ $res/@size }'/>
              let $headers := (
                <resource>Name</resource>,
                <type>Content type</type>,
                <raw>Raw</raw>,
                <size type='number' order='desc'>Size</size>
              )
              let $buttons := (
                html:button('add', 'Add…'),
                html:button('delete', 'Delete', true()),
                html:button('copy', 'Copy…', false()),
                html:button('alter-db', 'Rename…', false()),
                html:button('optimize', 'Optimize…', false(), 'global')
              )
              let $map := map { 'name': $name }
              let $link := function($value) { $dba:SUB }
              return html:table($headers, $rows, $buttons, $map,
                map { 'sort': $sort, 'link': $link, 'page': $page, 'count': $count })
            )
          }
        </form>
        <form action="{ $dba:SUB }" method="post" class="update">
          <input type="hidden" name="name" value="{ $name }"/>
          <h3>Backups</h3>
          {
            let $rows :=
              for $backup in $data/self::backup
              order by $backup descending
              return <row backup='{ $backup }' size='{ $backup/@size }'/>
            let $headers := (
              <backup order='desc'>Name</backup>,
              <size type='bytes'>Size</size>
            )
            let $buttons := (
              html:button('create-backup', 'Create', false(), 'global') update (
                if($only-backups) then insert node attribute disabled { '' } into . else ()
              ),
              html:button('restore', 'Restore', true()),
              html:button('drop-backup', 'Drop', true())
            )
            let $map := map { 'name': $name }
            let $link := function($value) { 'backup/' || $value || '.zip' }
            return html:table($headers, $rows, $buttons, $map, map { 'link': $link })
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
          <h4>Enter your query…</h4>
          <input style="width:100%" name="input" id="input" onkeyup='queryResource(false)'/>
          <div class='small'/>
          { html:focus('input') }
          <textarea name='output' id='output' rows='20' readonly='' spellcheck='false'/>
          <script type="text/javascript">
            loadCodeMirror();
            queryResource(true);
          </script>
        </_>/node() else (
          $data/self::database ! html:properties(.)
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
