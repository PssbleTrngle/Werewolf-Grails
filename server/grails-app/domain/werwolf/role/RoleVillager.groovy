package werwolf.role;

import werwolf.Game;
import werwolf.Role;

class RoleVillager extends Role {

    @Override
    boolean triggerWin(Game game) {
        game.alive().count({!it.getRole().instanceOf(RoleWerewolf.class)}) == 0
    }

}
