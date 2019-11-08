package werwolf.screen

import werwolf.User

class ScreenEnd extends ScreenStatic {

    boolean won

    @Override
    String getMessage() {
        won ? 'You won' : 'You lost'
    }

    @Override
    protected boolean isVoter(User user, User self) {
        return user.unwrapRole().hasWon(user.game) == won
    }

    @Override
    String getKey() {
        return 'end'
    }

}
