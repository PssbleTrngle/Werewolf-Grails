package werwolf

import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

class Game {

    static final MIN_PLAYERS = 3

    boolean started
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

    boolean isOpen() {
        return !isStarted()
    }

    void checkScreens() {

        getScreens().each({ Vote screen ->
            if(screen.canClose())
                User.withTransaction({

                    screen.getUsers().each({ User voter ->
                        if (voter.screen == null || voter.screen.id == screen.id) {
                            Action next = screen.action().nextAction(voter)
                            if(next) voter.setNextAction(next)
                        }
                    })

                })
        })
    }

    Set<User> alive() {
        this.getUsers().findAll({User user -> !user.isDead()})
    }

    boolean hasOpen() {
        screens.count({ Vote screen -> screen.isOpen() }) > 0
    }

    void checkDone() {

        checkScreens()

        if (!hasOpen()) {
            /* TODO sort votes by priority */

            screens.each({ Vote screen ->

                def result = screen.calculateResult()

                if (screen.action()) User.withTransaction({
                    screen.action().run(alive(), result)
                })

            })
        }

        /* If there have not been created any new screens (ex.: the Hunter on death) */
        if (!hasOpen()) {
            screens.each({ Vote screen ->

                User.withTransaction({
                    screen.users.each({User user ->
                        if(user.screen?.id == screen.id) user.setScreen(null)
                        user.save()
                    })
                })

                screen?.decisions?.clear()
                screen?.users?.clear()
                screen.setGame(null)

            })

            screens.clear()

            Role won = hasWon()
            if(won) {
                int wonCount = getUsers().count({ it.unwrapRole().hasWon(this) })
                String verb = wonCount == 1 ? 'has' : 'have'
                String winMessage = "The ${won.getName()} $verb won"

                println winMessage
            }

            users.each({ User user ->

                Action action = Action.get(isIsNight() ? 'lynch' : (user.role?.nightAction ?: 'sleep'))
                if(user.isDead()) action = Action.get('dead')

                if(won) action = Action.get(user.getRole().hasWon(this) ? 'won' : 'lost')

                user.setNextAction(action)

            })

            withTransaction({
                setIsNight(!isIsNight())
                save()
            })

        }
    }

    Role hasWon() {
        for(User user : alive()) {
            Role role = user.unwrapRole()
            if(role.triggerWin(this)) return role
        }

        null
    }

    void assignRoles() {

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
                user.save()
            })

            this.save()

        })

    }

    void createChats() {

        Chat.withTransaction({

            Chat all = new Chat(name: 'Village')
            all.save()

            users.forEach({ User u1 ->
                all.addToUsers(u1)
                users.findAll({ it.id > u1.id }).forEach({ User u2 ->

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

    boolean isReady() {
        return this.getUsers().size() >= MIN_PLAYERS
    }

    Game start() {
        assert !isStarted()

        setStarted(true)
        assignRoles()
        createChats()
        save()

        this
    }

    void join(User user) {
        addToUsers(user)
        user.setGame(this)
        user.save()
        save()
        if(isReady()) start()
    }

    static Game createGame(List<User> users) {
        Game game = new Game()
        game.save()

        users.each({user ->
            game.addToUsers(user)
            user.setGame(game)
            user.save()
        })
        game.save()
    }

}
