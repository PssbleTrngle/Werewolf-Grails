package werwolf.screen

import werwolf.User
import werwolf.Screen

abstract class ScreenStatic extends Screen {

    @Override
    String[] options() { new String[0] }

    @Override
    Screen next(User user) { null }

    @Override
    void run(Set<User> users, def selection) {}

    @Override
    protected boolean isTarget(User user, User self) {
        false
    }

    @Override
    protected boolean isVoter(User user, User self) {
        user.id == self.id
    }
}
