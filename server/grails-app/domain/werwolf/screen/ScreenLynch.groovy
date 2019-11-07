package werwolf.screen

import werwolf.User

class ScreenLynch extends ScreenKill {

    @Override
    String getMessage() {
        'Whose head should roll?'
    }

    @Override
    protected boolean isTarget(User user, User self) {
        user.id != self.id
    }

    @Override
    protected boolean isVoter(User user, User self) {
        true
    }

    @Override
    String getKey() {
        return 'lynch'
    }

}
