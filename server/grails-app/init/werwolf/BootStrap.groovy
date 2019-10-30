package werwolf

import werwolf.role.RoleHunter
import werwolf.role.RoleVillager
import werwolf.role.RoleWerewolf

class BootStrap {

    def init = { servletContext ->

        if (Role.count() == 0) {
            new RoleVillager(name: 'villager').save()
            new RoleVillager(name: 'seer', nightAction: 'see').save()
            new RoleVillager(name: 'witch').save()
            new RoleWerewolf(name: 'werewolf', nightAction: 'eat').save()
            new RoleWerewolf(name: 'lonely_werewolf', nightAction: 'eat').save()
            new RoleHunter(name: 'hunter', deathAction: 'hunt').save()
        }

        Action.init()
    }
    def destroy = {
    }
}
