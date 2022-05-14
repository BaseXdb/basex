(:~
 : Jobs page.
 :
 : @author Christian Grün, BaseX Team 2005-22, BSD License
 :)
module namespace dba = 'dba/jobs';

import module namespace html = 'dba/html' at '../lib/html.xqm';
import module namespace util = 'dba/util' at '../lib/util.xqm';

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
  %rest:path('/dba/jobs')
  %rest:query-param('sort',  '{$sort}', 'duration')
  %rest:query-param('job',   '{$job}')
  %rest:query-param('error', '{$error}')
  %rest:query-param('info',  '{$info}')
  %output:method('html')
function dba:jobs(
  $sort   as xs:string,
  $job    as xs:string?,
  $error  as xs:string?,
  $info   as xs:string?
) as element(html) {
  html:wrap(map { 'header': $dba:CAT, 'info': $info, 'error': $error },
    <tr>{
      <td>
        <form action='{ $dba:CAT }' method='post' class='update'>
        <h2>Jobs</h2>
        {
          let $headers := (
            map { 'key': 'id', 'label': 'ID' },
            map { 'key': 'type', 'label': 'Type' },
            map { 'key': 'state', 'label': 'State' },
            map { 'key': 'duration', 'label': 'Dur.', 'type': 'number', 'order': 'desc' },
            map { 'key': 'user', 'label': 'User' },
            map { 'key': 'you', 'label': 'You' },
            map { 'key': 'time', 'label': 'Time', 'type': 'time', 'order': 'desc' },
            map { 'key': 'start', 'label': 'Start', 'type': 'time', 'order': 'desc' }
          )
          let $entries :=
            let $curr := jobs:current()
            for $details in jobs:list-details()
            let $id := $details/@id
            let $sec := (
              let $dur := xs:dayTimeDuration($details/@duration)
              return if(exists($dur)) then $dur div xs:dayTimeDuration('PT1S') else 0
            )
            let $time := data($details/@time)
            let $start := data($details/@start)
            order by $sec descending, $start descending
            return map {
              'id': $id,
              'type': $details/@type,
              'state': $details/@state,
              'duration': html:duration($sec),
              'user': $details/@user,
              'you': if($id = $curr) then '✓' else '–',
              'time': $time,
              'start': $start ?: $time
            }
          let $buttons := (
            html:button('job-stop', 'Stop', true())
          )
          let $options := map { 'sort': $sort, 'presort': 'duration' }
          return html:table($headers, $entries, $buttons, map { }, $options) update {
            (: replace job ids with links :)
            for $tr at $p in tr[not(th)]
            for $entries in $entries[$p][?you = '–']
            let $text := $tr/td[1]/text()
            return replace node $text with <a href='?job={ $entries?id }'>{ $text }</a>
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
            <h2>Job: { $job }</h2>,
            if($details) then (
              <form action='jobs' method='post' id='jobs'>
                <input type='hidden' name='id' value='{ $job }'/>
                {
                  let $disabled := map { 'disabled': '' }
                  return (
                    html:button('job-stop', 'Stop', true(), $disabled[$cached]), ' ',
                    html:button('job-result', 'Download', false(), $disabled[not($cached)]), ' ',
                    html:button('job-discard', 'Discard', false(), $disabled[not($cached)]), ' '
                  )
                }
              </form>,
              <h3>General Information</h3>,
              <table>{
                for $value in $details/@*
                for $name in name($value)[. != 'id']
                return <tr>
                  <td><b>{ util:capitalize($name) }</b></td>
                  <td>{ string($value) }</td>
                </tr>
              }</table>,
              let $bindings := jobs:bindings($job)
              where map:size($bindings) > 0
              return (
                <h3>Query Bindings</h3>,
                <table>{
                  map:for-each($bindings, function($key, $value) {
                    <tr>
                      <td><b>{ $key ?? '$' || $key !! 'Context' }</b></td>
                      <td><code>{
                        util:chop(serialize($value, map { 'method': 'basex' }), 1000)
                      }</code></td>
                    </tr>
                  })
                }
                </table>
              ),
              <h3>Job String</h3>,
              <textarea readonly='' rows='10'>{
                string($details)
              }</textarea>
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
  %rest:path('/dba/jobs')
  %rest:query-param('action', '{$action}')
  %rest:query-param('id',     '{$ids}')
function dba:jobs-redirect(
  $action  as xs:string,
  $ids     as xs:string*
) as element(rest:response) {
  web:redirect($action, map { 'id': $ids })
};
