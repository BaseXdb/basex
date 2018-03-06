(:~
 : Jobs page.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/jobs';

import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'jobs';

(:~
 : Jobs page.
 : @param  $sort   table sort key
 : @param  $job    highlighted job
 : @param  $error  error message
 : @param  $info   info message
 : @return page
 :)
declare
  %rest:GET
  %rest:path("/dba/jobs")
  %rest:query-param("sort",  "{$sort}", "")
  %rest:query-param("job",   "{$job}")
  %rest:query-param("error", "{$error}")
  %rest:query-param("info",  "{$info}")
  %output:method("html")
function dba:jobs(
  $sort   as xs:string,
  $job    as xs:string?,
  $error  as xs:string?,
  $info   as xs:string?
) as element(html) {
  html:wrap(map { 'header': $dba:CAT, 'info': $info, 'error': $error },
    <tr>{
      <td>
        <form action="{ $dba:CAT }" method="post" class="update">
        <h2>Jobs</h2>
        {
          let $headers := (
            <id>ID</id>,
            <type>Type</type>,
            <state>State</state>,
            <dur type='number' order='desc'>Dur.</dur>,
            <user>User</user>,
            <you>You</you>
          )
          let $rows :=
            let $curr := jobs:current()
            for $details in jobs:list-details()
            let $id := $details/@id
            let $sec := (
              let $dur := xs:dayTimeDuration($details/@duration)
              return if(exists($dur)) then $dur div xs:dayTimeDuration('PT1S') else 0
            )
            let $you := if($id = $curr) then '✓' else '–'
            let $start := string($details/@start)
            let $end := string($details/@end)
            order by $sec descending, $start descending
            return <row id='{ $id }' type='{ $details/@type }' state='{ $details/@state }'
                        dur='{ html:duration($sec) }' user='{ $details/@user }' you='{ $you }'/>
          let $buttons := (
            html:button('job-stop', 'Stop', true())
          )
          return html:table($headers, $rows, $buttons, map { }, map { 'sort': $sort }) update {
            (: replace job ids with links :)
            for $tr at $p in tr[not(th)]
            for $row in $rows[$p][@you = '–']
            let $text := $tr/td[1]/text()
            return replace node $text with <a href='?job={ $row/@id }'>{ $text }</a>
          }
        }
        </form>
      </td>,
      if($job) then (
        let $details := jobs:list-details($job)
        let $cached := $details/@state = 'cached'
        return (
          <td class='vertical'/>,
          <td>{
            <h3>{ $job }</h3>,
            if($details) then (
              <form action="jobs" method="post" id="jobs">
                <input type="hidden" name="id" value="{ $job }"/>
                {
                  let $disabled := map { 'disabled': '' }
                  return (
                    html:button('job-stop', 'Stop', true(), $disabled[$cached]), ' ',
                    html:button('job-result', 'Download', false(), $disabled[not($cached)]), ' ',
                    html:button('job-discard', 'Discard', false(), $disabled[not($cached)]), ' '
                  )
                }
              </form>,
              <table>{
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
          }</td>
        )
      ) else()
    }</tr>
  )
};

(:~
 : Redirects to the specified action.
 : @param  $action  action to perform
 : @param  $ids     ids
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path("/dba/jobs")
  %rest:query-param("action", "{$action}")
  %rest:query-param("id",     "{$ids}")
function dba:jobs-redirect(
  $action  as xs:string,
  $ids     as xs:string*
) as element(rest:response) {
  web:redirect($action, map { 'id': $ids })
};
