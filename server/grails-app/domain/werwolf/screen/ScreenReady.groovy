package werwolf.screen

import werwolf.Screen
import werwolf.User

class ScreenReady extends Screen {

    @Override
    String getMessage() {
        'Are you ready?'
    }

    @Override
    void run(Set<User> users, Object selection) {}

    @Override
    protected boolean isTarget(User user, User self) { false }

    @Override
    protected boolean isVoter(User user, User self) { true }

    @Override
    String getKey() {
        return 'ready'
    }

    @Override
    String[] options() { ['Yes'] }
}
