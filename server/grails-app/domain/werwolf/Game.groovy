package werwolf

class Game {

    int id
    boolean isNight = false

    /* Game Options */
    boolean revealDead = true
    boolean anonymousPolls = false

    static hasMany = [ users: User, screens: Vote ]

    static constraints = {}

    static mapping = {
        users sort: 'id'
        decisions cascade: 'all-delete-orphan'
    }

    void checkDone() {
        def users = this.getUsers().findAll({User user -> !user.isDead()})

        int unfinished = screens.count({ Vote screen ->
            !screen.isStatic() && screen.decisions?.size() < screen.users?.size()
        })

        int sleeping = users.count({ User user ->
            user.screen?.action == 'sleep'
        })

        if (unfinished == 0) {

            /* TODO sort votes by priority */
            screens.each({ Vote screen ->

                def result = screen.calculateResult()
                screen?.users?.clear()
                screen?.decisions?.clear()

                if (screen.action()) User.withTransaction({
                    screen.action().run(users, result)
                })

            })

            screens.clear()

            Game.withTransaction({
                setIsNight(!isIsNight())
                save()
            })

            if(sleeping == users.size()) {
                users.each({ User user ->
                    /* TODO insert pending screen */

                    Vote lynch = new Vote(action: 'lynch', game: this).save()
                    user.setProperty('screen', lynch)
                    user.save()
                })
            }

        }
    }

    void assignRoles(List<User> users) {

        Role villager = Role.get(1)
        Role werewolf = Role.get(4)
        Role[] supporters = [Role.get(2), Role.get(3)]

        List<Role> roles = []
        for(int i = 0; i < users.size() / 3; i++)
            roles.add(werewolf)

        supporters.each({ if(roles.size() < users.size()) roles.add(it) })

        while(roles.size() < users.size())
            roles.add(villager)

        //Collections.shuffle(roles)
        Vote.withTransaction({

            save()
            Vote ready = new Vote(action: 'ready', game: this).save()

            users.eachWithIndex({user, index ->
                user.setProperty('role', roles.get(index))
                user.setProperty('screen', ready)
                user.setProperty('game', this)
                user.save()
                addToUsers(user)
            })

            save()

        })

    }

}
