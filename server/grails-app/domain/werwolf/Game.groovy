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
        screens cascade: 'all-delete-orphan'
    }

    void checkScreens() {

        getScreens().each({ Vote screen ->
            if(screen.canClose())
                User.withTransaction({

                    screen.getUsers().each({ User voter ->
                        if (voter.screen == null || voter.screen.id == screen.id) {
                            Action next = screen.action().nextAction(voter)
                            println "Next: ${voter.screen?.action}; ${next?.name}"
                            if(next) voter.setNextAction(next)
                        }
                    })

                })
        })
    }

    void checkDone() {

        def alive = this.getUsers().findAll({User user -> !user.isDead()})
        checkScreens()

        int unfinished = screens.count({ Vote screen -> screen.isOpen() })

        if (unfinished == 0) {

            /* TODO sort votes by priority */
            screens.each({ Vote screen ->

                def result = screen.calculateResult()
                screen?.users?.clear()
                screen?.decisions?.clear()

                if (screen.action()) User.withTransaction({
                    screen.action().run(alive, result)
                })

            })

            screens.clear()

            alive.each({ User user ->
                /* TODO insert pending screen */

                Action action = Action.get(isIsNight() ? 'lynch' : (user.role?.nightAction ?: 'sleep'))
                if(user.isDead()) Action.get('dead')

                user.setNextAction(action)

            })

            withTransaction({
                setIsNight(!isIsNight())
                save()
            })

        }
    }

    void assignRoles(List<User> users) {

        Role villager = Role.findByName('villager')
        Role werewolf = Role.findByName('werewolf')
        Role[] supporters = [Role.findByName('seer'), Role.findByName('hunter')]

        List<Role> roles = []
        for(int i = 0; i < users.size() / 3; i++)
            roles.add(werewolf)

        supporters.each({ if(roles.size() < users.size()) roles.add(it) })

        while(roles.size() < users.size())
            roles.add(villager)

        Collections.shuffle(roles)
        Vote.withTransaction({

            Vote ready = new Vote(action: 'ready', game: this).save()

            users.eachWithIndex({user, index ->
                user.setRole(roles.get(index))
                user.setScreen(ready)
                this.addToUsers(user)
                user.save()
            })

            this.save()

        })

    }

    void createChats(List<User> users) {

        Chat.withTransaction({

            Chat all = new Chat(name: 'Village')
            all.save()

            users.forEach({ User u1 ->
                all.addToUsers(u1)
                users.forEach({ User u2 ->

                    Chat chat = new Chat()
                    chat.save()
                    u1.addToChats(chat)
                    u2.addToChats(chat)

                    u1.save()
                    u2.save()

            })})

            all.save()

        })


    }

    static Game createGame(List<User> users) {
        Game game = new Game()

        game.save()
        game.assignRoles(users)
        game.createChats(users)

        return game

    }

}
