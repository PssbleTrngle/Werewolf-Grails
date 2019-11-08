package werwolf

import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import werwolf.screen.ScreenDead
import werwolf.screen.ScreenEnd
import werwolf.screen.ScreenLynch
import werwolf.screen.ScreenReady
import werwolf.screen.ScreenSleep

class Game {

    static final MIN_PLAYERS = 3

    boolean started
    int id
    boolean isNight = false

    /* Game Options */
    boolean revealDead = true

    static hasMany = [ users: User, screens: Screen ]

    static constraints = {}

    static mapping = {
        users sort: 'id'
        screens cascade: 'all-delete-orphan'
    }

    boolean isOpen() {
        return !isStarted()
    }

    void checkScreens() {

        getScreens().each({ Screen screen ->
            screen = (Screen) GrailsHibernateUtil.unwrapIfProxy(screen)
            if(screen.canClose())
                User.withTransaction({

                    screen.getUsers().each({ User voter ->
                        if (voter.unwrapScreen() == null || voter.unwrapScreen().id == screen.id) {
                            Screen next = screen.nextScreen(voter)
                            if(next) voter.setNextScreen(next)
                        }
                    })

                })
        })
    }

    Set<User> alive() {
        this.getUsers().findAll({User user -> !user.isDead()})
    }

    boolean hasOpen() {
        screens.count({ Screen screen -> screen.isOpen() }) > 0
    }

    void checkDone() {

        checkScreens()

        if (!hasOpen()) {
            /* TODO sort votes by priority */

            screens.each({ Screen screen ->
                screen = (Screen) GrailsHibernateUtil.unwrapIfProxy(screen)

                def result = screen.calculateResult()

                User.withTransaction({
                    screen.run(alive(), result)
                })

            })
        }

        /* If there have not been created any new screens (ex.: the Hunter on death) */
        if (!hasOpen()) {
            screens.each({ Screen screen ->

                User.withTransaction({
                    screen.users.each({User user ->
                        if(user.unwrapScreen()?.id == screen.id) user.setScreen(null)
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

                Screen next
                if(won)
                    next = new ScreenEnd(won: user.getRole().hasWon(this))
                else if(user.isDead())
                    next = new ScreenDead()
                else if (isIsNight())
                    next = new ScreenLynch()
                else
                    next = (user.unwrapRole().nightScreen() ?: new ScreenSleep())

                user.setNextScreen(next)

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

        /* supporters.each({ if(roles.size() < users.size()) roles.add(it) }) */

        while(roles.size() < users.size())
            roles.add(villager)

        Collections.shuffle(roles)
        Screen.withTransaction({

            Screen ready = new ScreenReady()
            ready.game = this
            ready.save(failOnError: true)

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
        assert user.getGame() == null : 'User is already in a game'

        addToUsers(user)
        user.setGame(this)
        user.save()
        save()
        if(isReady()) start()
    }

    static Game createGame(User... users) {
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
