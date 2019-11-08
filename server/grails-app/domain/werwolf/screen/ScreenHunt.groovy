package werwolf.screen

import werwolf.User

class ScreenHunt extends ScreenKill {

    @Override
    String getMessage() {
        'Who will you take with you?'
    }

    @Override
    protected boolean isTarget(User user, User self) {
        return user.id != self.id
    }

    @Override
    protected boolean isVoter(User user, User self) {
        return user.id == self.id
    }

    @Override
    String getKey() {
        return 'hunt'
    }

}
