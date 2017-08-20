(:~
 : Files page.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/files';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'files';

(:~
 : Files page.
 : @param  $sort   table sort key
 : @param  $job    highlighted job
 : @param  $error  error message
 : @param  $info   info message
 : @return page
 :)
declare
  %rest:GET
  %rest:path("/dba/files")
  %rest:query-param("sort",  "{$sort}", "")
  %rest:query-param("job",   "{$job}")
  %rest:query-param("error", "{$error}")
  %rest:query-param("info",  "{$info}")
  %output:method("html")
function dba:files(
  $sort   as xs:string,
  $job    as xs:string?,
  $error  as xs:string?,
  $info   as xs:string?
) as element(html) {
  cons:check(),

  let $dir := $cons:DBA-DIR ! (file:create-dir(.), .)
  return html:wrap(map { 'header': $dba:CAT, 'info': $info, 'error': $error },
    <tr>
      <td width='54%'>
        <form action="{ $dba:CAT }" method="post" class="update">
        <h2>Files</h2>
        {
          let $headers := (
            <name>Name</name>,
            <date type='dateTime' order='desc'>Date</date>,
            <bytes type='bytes' order='desc'>Bytes</bytes>,
            <action type='xml'>Action</action>
          )
          let $rows := (
            let $files := file:children($dir)[file:is-file(.)]
            let $jobs := jobs:list-details()
            for $file in $files
            let $name := file:name($file)
            let $date := file:last-modified($file)
            let $xquery := ends-with($name, $cons:SUFFIX)
            let $actions := util:item-join(if($xquery) then (
              html:link('Edit', 'queries',
                map { 'file': replace($name, $cons:SUFFIX || '$', '') }
              ),
              let $job := (
                let $uri := file:path-to-uri($file)
                return $jobs[. = $uri]
              )
              let $id := string($job/@id)
              return if($job) then (
                html:link('Stop', 'job-stop', map { 'id': $id })
              ) else (
                html:link('Start', 'job-start', map { 'file': $name })
              )
            ) else (), ' | ')
            return <row name='{ $name }' date='{ $date }' bytes='{ file:size($file) }'
              action='{ serialize($actions) }'/>
          )
          let $buttons := html:button('file-delete', 'Delete', true())
          let $link := function($value) { 'file/' || $value }
          return html:table($headers, $rows, $buttons, map { }, map { 'sort': $sort,
            'link': $link })
        }
        </form>
        <h3>Upload Files</h3>
        <form action="file-upload" method="post" enctype="multipart/form-data">
          <input type="file" name="files" multiple="multiple"/>
          <input type="submit" value='Send'/>
        </form>
        <div class='note'>
          DBA directory: <code>{ file:path-to-native($dir) }</code>
        </div>
        <div>&#xa0;</div>
      </td>
      <td class='vertical'/>
      <td width='44%'>
        <form action="{ $dba:CAT }" method="post" class="update">
        <h2>Jobs</h2>
        {
          let $headers := (
            <id>ID</id>,
            <type>Type</type>,
            <state>State</state>,
            <dur type='number' order='desc'>Dur.</dur>,
            <you>You</you>
          )
          let $rows :=
            let $curr := jobs:current()
            for $details in jobs:list-details()
            let $id := $details/@id
            let $ms := xs:dayTimeDuration($details/@duration) div xs:dayTimeDuration('PT0.001S')
            let $you := if($id = $curr) then '✓' else '–'
            let $start := string($details/@start)
            let $end := string($details/@end)
            order by $ms descending, $start descending
            return <row id='{ $id }' type='{ $details/@type }' state='{ $details/@state }'
                        dur='{ $ms div 1000 }' you='{ $you }' user='{ $details/@user }'
                        reads='{ $details/@reads }' writes='{ $details/@writes }'
                        start='{ $start}' end='{ $end }'/>
          let $buttons := (
            html:button('job-stop', 'Stop', true())
          )
          return html:table($headers, $rows, $buttons, map { }, map { }) update {
            (: replace job ids with links :)
            for $tr at $p in tr[not(th)]
            for $row in $rows[$p][@you = '–']
            let $text := $tr/td[1]/text()
            return replace node $text with <a href='?job={ $row/@id }'>{ $text }</a>
          },
          if($job) then (
            <div class='small'/>,
            <h3>{ $job }</h3>,
            let $details := jobs:list-details($job)
            return if($details) then (
              <table>{
                <tr>
                  <td><b>Result</b></td>
                  <td>{
                    if($details/@state = 'cached') then (
                      html:link($job || '.txt', 'job-result/' || web:encode-url($job || '.txt'),
                        map { 'id': $job }
                      )
                    ) else 'not available yet'
                  }</td>
                </tr>,
                for $value in $details/@*
                for $name in name($value)[. != 'id']
                return <tr>
                  <td><b>{ util:capitalize($name) }</b></td>
                  <td>{ string($value) }</td>
                </tr>,
                <tr>
                  <td><b>Description</b></td>
                  <td>{ util:chop($details, 500) }</td>
                </tr>
              }</table>
            ) else (
              'Job is defunct.'
            )
          ) else()
        }
        </form>
      </td>
    </tr>
  )
};

(:~
 : Redirects to the specified action.
 : @param  $action  action to perform
 : @param  $names   names of files
 : @param  $ids     ids
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path("/dba/files")
  %rest:query-param("action", "{$action}")
  %rest:query-param("name",   "{$names}")
  %rest:query-param("id",     "{$ids}")
  %output:method("html")
function dba:files-redirect(
  $action  as xs:string,
  $names   as xs:string*,
  $ids     as xs:string*
) as element(rest:response) {
  cons:check(),
  web:redirect($action,
    if($action = ('job-stop')) then map { 'id': $ids }
    else map { 'name': $names, 'redirect': $dba:CAT }
  )
};
