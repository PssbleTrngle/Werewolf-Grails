package werwolf

class BootStrap {

    def init = { servletContext ->

        if (Role.count() == 0) {
            new Role(name: 'villager').save()
            new Role(name: 'seer', nightAction: 'see').save()
            new Role(name: 'witch').save()
            new Role(name: 'werewolf', nightAction: 'eat').save()
            new RoleHunter(name: 'hunter', deathAction: 'hunt').save()
        }

        Action.init()
    }
    def destroy = {
    }
}
