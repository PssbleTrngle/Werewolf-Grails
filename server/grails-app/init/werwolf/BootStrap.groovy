package werwolf

class BootStrap {

    def init = { servletContext ->

        if (Role.count() == 0) {
            new Role(id: 1, name: 'Villager').save()
            new Role(id: 2, name: 'Seer', nightAction: 'see').save()
            new Role(id: 3, name: 'Witch').save()
            new Role(id: 4, name: 'Werewolf', nightAction: 'eat').save()
        }

        Action.init()
    }
    def destroy = {
    }
}
