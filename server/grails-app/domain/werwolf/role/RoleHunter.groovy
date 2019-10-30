package werwolf.role

import werwolf.Action
import werwolf.Role
import werwolf.User
import werwolf.Vote

class RoleHunter extends RoleVillager {

    @Override
    boolean onDeath(User user) {
        println "Before: ${Vote.count()}"
        user.setNextAction(Action.get('hunt'))
        println "After: ${Vote.count()}"
        false
    }
}
