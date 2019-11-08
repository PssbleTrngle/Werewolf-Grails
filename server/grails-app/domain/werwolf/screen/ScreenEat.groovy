package werwolf.screen

import werwolf.Role
import werwolf.User
import werwolf.role.RoleWerewolf

class ScreenEat extends ScreenKill {

    @Override
    String getMessage() {
        'Who will you eat?'
    }

    @Override
    protected boolean isTarget(User user, User self) {
        return !user.getRole().instanceOf(RoleWerewolf.class)
    }

    @Override
    protected boolean isVoter(User user, User self) {
        return user.getRole().instanceOf(RoleWerewolf.class)
    }

    @Override
    String getKey() {
        return 'eat'
    }

    @Override
    Role displayAs() {
        return Role.findByName('Werewolf')
    }
}
