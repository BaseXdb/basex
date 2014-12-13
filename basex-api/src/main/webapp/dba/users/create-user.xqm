(:~
 : Create new user.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace _ = 'dba/users';

import module namespace G = 'dba/global' at '../modules/global.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../modules/tmpl.xqm';
import module namespace web = 'dba/web' at '../modules/web.xqm';

(:~ Top category :)
declare variable $_:CAT := 'users';

(:~
 : Form for creating a new user.
 : @param  $name   entered name
 : @param  $pw     entered password
 : @param  $perm   chosen permission
 : @param  $error  error string
 :)
declare
  %rest:GET
  %rest:path("dba/create-user")
  %rest:query-param("name",  "{$name}")
  %rest:query-param("pw",    "{$pw}")
  %rest:query-param("perm",  "{$perm}", "none")
  %rest:query-param("error", "{$error}")
  %output:method("html")
function _:create(
  $name   as xs:string?,
  $pw     as xs:string?,
  $perm   as xs:string,
  $error  as xs:string?
) as element() {
  web:check(),
  tmpl:wrap(map { 'top': $_:CAT, 'error': $error },
    <tr>
      <td>
        <form action="create-user" method="post" autocomplete="off">
          <!--  force chrome not to autocomplete form -->
          <input style="display:none" type="text" name="fake1"/>
          <input style="display:none" type="password" name="fake2"/>
          <h2>
            <a href="{ $_:CAT }">Users</a> »
            { html:button('create', 'Create') }
          </h2>
          <!-- dummy value; prevents reset of options when nothing is selected -->
          <input type="hidden" name="opts" value="x"/>
          <table>
            <tr>
              <td>Name:</td>
              <td>
                <input type="text" name="name" value="{ $name }" id="name"/>
                { html:focus('name') }
                <div class='small'/>
              </td>
            </tr>
            <tr>
              <td>Passsword:</td>
              <td>
                <input type="password" name="pw" value="{ $pw }" id="pw"/>
                <div class='small'/>
              </td>
            </tr>
            <tr>
              <td>Permission:</td>
              <td>
                <select name="perm" size="5">{
                  for $p in $G:PERMISSIONS
                  return element option { attribute selected { }[$p = $perm], $p }
                }</select>
                <div class='small'/>
              </td>
            </tr>
          </table>
        </form>
      </td>
    </tr>
  )
};

(:~
 : Creates a user.
 : @param  $name  user
 : @param  $pw    password
 : @param  $perm  permission
 : @param  $lang  language
 :)
declare
  %updating
  %rest:POST
  %rest:path("dba/create-user")
  %rest:query-param("name", "{$name}")
  %rest:query-param("pw",   "{$pw}")
  %rest:query-param("perm", "{$perm}")
function _:create(
  $name  as xs:string,
  $pw    as xs:string,
  $perm  as xs:string
) {
  web:check(),
  try {
    web:update("if(user:exists($name)) then (
      error((), 'User already exists: ' || $name || '.')
    ) else (
      user:create($name, $pw, $perm)
    )", map {
      'name': $name,
      'pw':   $pw,
      'perm': $perm
    }),
    web:redirect($_:CAT, map {
      'info': 'Created User: ' || $name,
      'name': $name
    })
  } catch * {
    web:redirect("create-user", map {
      'error': $err:description,
      'name': $name,
      'pw':   $pw,
      'perm': $perm
    })
  }
};
