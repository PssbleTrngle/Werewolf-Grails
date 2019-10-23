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

    void checkScreens() {

        getScreens().each({ Vote screen ->
            if(screen.canClose())
                User.withTransaction({

                    def users = screen.getUsers()
                    users.clear()

                    users.each({ User voter ->
                        if (voter.screen == null) {

                            Action next = screen.action().nextAction(voter)
                            if(next) voter.setNextAction(next)
                        }
                    })
                })
        })
    }

    void checkDone() {

        checkScreens()

        def users = this.getUsers().findAll({User user -> !user.isDead()})
        int unfinished = screens.count({ Vote screen -> screen.isOpen() })

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

            users.each({ User user ->
                /* TODO insert pending screen */

                Action action = Action.get(isIsNight() ? 'lynch' : (user.role?.nightAction ?: 'sleep'))
                user.setNextAction(action)

            })

            withTransaction({
                setIsNight(!isIsNight())
                save()
            })

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
