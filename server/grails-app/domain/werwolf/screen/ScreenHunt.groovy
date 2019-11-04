package werwolf.screen

import werwolf.User

class ScreenLynch extends ScreenKill {

    ScreenLynch() {
        super('Whose head should roll?')
    }

    @Override
    protected boolean isTarget(User user, User self) {
        return false
    }

    @Override
    protected boolean isVoter(User user, User self) {
        return false
    }

    @Override
    String getKey() {
        return 'lynch'
    }

}
