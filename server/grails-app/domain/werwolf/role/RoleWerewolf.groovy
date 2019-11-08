package werwolf.role

import werwolf.Game
import werwolf.Role
import werwolf.Screen
import werwolf.screen.ScreenEat

class RoleWerewolf extends Role {

    @Override
    boolean triggerWin(Game game) {
        game.alive().count({!it.unwrapRole().instanceOf(RoleWerewolf.class)}) == 0
    }

    @Override
    Screen nightScreen() {
        new ScreenEat()
    }

}
