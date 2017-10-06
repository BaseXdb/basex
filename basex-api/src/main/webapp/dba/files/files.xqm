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
      <td>
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
            let $actions := util:item-join((
              html:link('Download', 'file/' || encode-for-uri($name)),
              if($xquery) then (
                html:link('Edit', 'queries',
                  map { 'file': replace($name, $cons:SUFFIX || '$', '') }
                ),
                let $job := (
                  let $uri := file:path-to-uri($file)
                  return $jobs[. = $uri]
                )
                let $id := string($job/@id)
                return if(empty($job)) then (
                  html:link('Start', 'file-start', map { 'file': $name })
                ) else (
                  html:link('Job', 'jobs', map { 'job': $id })
                )
              ) else ()
            ), ' · ')
            return <row name='{ $name }' date='{ $date }' bytes='{ file:size($file) }'
              action='{ serialize($actions) }'/>
          )
          let $buttons := html:button('file-delete', 'Delete', true())
          return html:table($headers, $rows, $buttons, map { }, map { 'sort': $sort })
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
function dba:files-redirect(
  $action  as xs:string,
  $names   as xs:string*
) as element(rest:response) {
  cons:check(),
  web:redirect($action, map { 'name': $names, 'redirect': $dba:CAT })
};
