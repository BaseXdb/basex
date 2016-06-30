(:~
 : User main page.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace _ = 'dba/jobs-users';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../modules/tmpl.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $_:CAT := 'jobs-users';
(:~ Sub category :)
declare variable $_:SUB := 'user';

(:~
 : Manage a single user.
 : @param  $name     user
 : @param  $newname  new name
 : @param  $pw       password
 : @param  $perm     permission
 : @param  $error    error string
 : @param  $info     info string
 : @return page
 :)
declare
  %rest:GET
  %rest:path("/dba/user")
  %rest:query-param("name",     "{$name}")
  %rest:query-param("newname",  "{$newname}")
  %rest:query-param("pw",       "{$pw}")
  %rest:query-param("perm",     "{$perm}")
  %rest:query-param("error",    "{$error}")
  %rest:query-param("info",     "{$info}")
  %output:method("html")
function _:user(
  $name     as xs:string,
  $newname  as xs:string?,
  $pw       as xs:string?,
  $perm     as xs:string?,
  $error    as xs:string?,
  $info     as xs:string?
) as element() {
  cons:check(),

  let $data := try {
    util:eval('element result {
      user:list-details($n)
    }', map { 'n': $name })
  } catch * {
    element error { $cons:DATA-ERROR || ': ' || $err:description }
  }
  let $error := ($data/self::error/string(), $error)[1]
  let $found := exists($data/user)
  let $admin := $name eq 'admin'

  return tmpl:wrap(map { 'top': $_:CAT, 'info': $info, 'error': $error },
    <tr>
      <td width='49%'>
        <form action="edit-user" method="post" autocomplete="off">
          <!--  force chrome not to autocomplete form -->
          <input style="display:none" type="text" name="fake1"/>
          <input style="display:none" type="password" name="fake2"/>
          <h2>
            <a href="{ $_:CAT }">Users</a> » { $name } »
            { html:button('save', 'Save') }
          </h2>
          <input type="hidden" name="name" value="{ $name }"/>
          <table>{
            let $admin := $name eq 'admin' return (
              if($admin) then <input type='hidden' name='newname' value='admin'/> else (
                <tr>
                  <td>Name:</td>
                  <td>
                    <input type="text" name="newname"
                      value="{ ($newname, $name)[1] }" id="newname"/>
                    { html:focus('newname') }
                    <div class='small'/>
                  </td>
                </tr>
              ),
              <tr>
                <td>Passsword:</td>
                <td>
                  <input type="password" name="pw" value="{ $pw }" id="pw"/> &#xa0;
                  <span class='note'>
                    …only changed if a new one is entered<br/>
                  </span>
                  <div class='small'/>
                </td>
              </tr>,
              if($admin) then <input type='hidden' name='perm' value='admin'/> else (
                <tr>
                  <td>Permission:</td>
                  <td>
                    <select name="perm" size="5">{
                      let $perm := ($perm, $data/user/@permission)[1]
                      for $p in $cons:PERMISSIONS
                      return element option { attribute selected { }[$p = $perm], $p }
                    }</select>
                    <div class='small'/>
                  </td>
                </tr>
              )
            )
          }</table>
        </form>
      {
        if($admin) then () else <_>
          <hr/>
          <h3>Local Permissions</h3>
          <form action="{ $_:SUB }" method="post" id="{ $_:SUB }" class="update">
            <input type="hidden" name="name" value="{ $name }" id="name"/>
            <div class='small'/>
            {
              if(not($found)) then () else (
                let $entries := $data/user/database/<e pattern='{ @pattern }' perm='{ @permission }'/>
                let $headers := (
                  <pattern>{ html:label($entries, ('Pattern', 'Patterns')) }</pattern>,
                  <perm>Local Permission</perm>
                )
                let $buttons := if($admin) then () else (
                  html:button('add-pattern', 'Add…'),
                  html:button('drop-pattern', 'Drop', true())
                )
                return html:table($entries, $headers, $buttons)
              )
            }
          </form>
          <div class='note'>
            A global permission can be overwritten by a local permission.<br/>
            Local permissions are applied to those databases that match<br/>
            a specified pattern. The pattern uses the
            <a target='_blank' href='http://docs.basex.org/wiki/Commands#Glob_Syntax'>glob syntax</a>.<br/>
          </div>
        </_>/node()
      }</td>
    </tr>
  )
};

(:~
 : Redirects to the specified action.
 : @param  $action   action to perform
 : @param  $name     user
 : @param  $pattern  pattern
 :)
declare
  %rest:POST
  %rest:path("/dba/user")
  %rest:form-param("action",  "{$action}")
  %rest:form-param("name",    "{$name}")
  %rest:form-param("pattern", "{$pattern}")
function _:action(
  $action   as xs:string,
  $name     as xs:string,
  $pattern  as xs:string?
) {
  web:redirect($action, map { 'name': $name, 'pattern': $pattern })
};

(:~
 : Edits a user.
 : @param  $name     user
 : @param  $newname  new name
 : @param  $pw       password
 : @param  $perm     permission
 : @param  $lang     language
 :)
declare
  %updating
  %rest:POST
  %rest:path("/dba/edit-user")
  %rest:query-param("name",    "{$name}")
  %rest:query-param("newname", "{$newname}")
  %rest:query-param("pw",      "{$pw}")
  %rest:query-param("perm",    "{$perm}")
function _:create(
  $name     as xs:string,
  $newname  as xs:string,
  $pw       as xs:string,
  $perm     as xs:string
) {
  cons:check(),
  try {
    util:update("
      let $old := user:list-details($name) return (
        if($name = $newname) then () else if(user:exists($newname)) then (
           error((), 'User already exists: ' || $newname || '.')
         ) else (
           user:alter($name, $newname)
        ),
        if($pw = '') then () else user:password($name, $pw),
        if($perm = $old/@permission) then () else user:grant($name, $perm)
      )",
      map { 'name': $name, 'newname': $newname, 'pw': $pw, 'perm': $perm }
    ),
    db:output(web:redirect($_:SUB, map {
      'info': 'Changes saved.',
      'name': $newname
    }))
  } catch * {
    db:output(web:redirect($_:SUB, map {
      'error': $err:description,
      'name': $name,
      'newname': $newname,
      'pw':   $pw,
      'perm': $perm
    }))
  }
};
