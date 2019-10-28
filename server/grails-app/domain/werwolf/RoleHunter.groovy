package werwolf

class RoleHunter extends Role {

    @Override
    boolean onDeath(User user) {
        println "Before: ${Vote.count()}"
        user.setNextAction(Action.get('hunt'))
        println "After: ${Vote.count()}"
        false
    }
}
