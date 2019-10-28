package werwolf

class RoleHunter extends Role {

    @Override
    boolean onDeath(User user) {
        user.setNextAction(Action.get('hunt'))
        false
    }
}
