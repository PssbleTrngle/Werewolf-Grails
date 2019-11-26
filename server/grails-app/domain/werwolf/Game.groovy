package werwolf

import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import werwolf.screen.ScreenDead
import werwolf.screen.ScreenEnd
import werwolf.screen.ScreenLynch
import werwolf.screen.ScreenReady
import werwolf.screen.ScreenSleep

/**
*   A game between multiple users
*/
class Game {

    static final MIN_PLAYERS = 5

    boolean started
    int id
    boolean isNight = false

    /* Game Options (WIP) */
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
            /* Neccessary to access inheriting Screen class and its overwritten methods */
            screen = (Screen) GrailsHibernateUtil.unwrapIfProxy(screen)
            if(screen.canClose())
                User.withTransaction({

                    /* Move all users on this screen to their next one */
                    screen.getUsers().each({ User voter ->
                        if (voter.unwrapScreen() == null || voter.unwrapScreen().id == screen.id) {
                            Screen next = screen.nextScreen(voter)
                            if(next) voter.setNextScreen(next)
                        }
                    })

                })
        })
    }


    /**
    *   @returns all users still alive
    */
    Set<User> alive() {
        this.getUsers().findAll({User user -> !user.isDead()})
    }

    /**
    *   @returns if there are any screens still needing interaction of a user
    */
    boolean hasOpen() {
        screens.count({ Screen screen -> screen.isOpen() }) > 0
    }


    /**
    *   Checks if the game can begin the next cycle (day/night) and enters it
    */
    void checkDone() {

        checkScreens()

        if (!hasOpen()) {
            /* TODO sort votes by priority */

            /* Execute all screens */
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

            /* Remove all screens */
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

                /* Proceed by putting all users on their first screen of their next cycle */

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

    /**
    *   @returns The role, if exisiting, that has won the game
    */
    Role hasWon() {
        for(User user : alive()) {
            Role role = user.unwrapRole()
            if(role.triggerWin(this)) return role
        }

        null
    }

    /**
    *   Assignes everybody a random role from a calculated set
    *   Called once at the start
    */
    void assignRoles() {

        Role villager = Role.findByName('villager')
        Role werewolf = Role.findByName('werewolf')
        Role[] supporters = [Role.findByName('seer'), Role.findByName('hunter')]

        /* A third of the village should be werewolves */
        List<Role> roles = []
        for(int i = 0; i < users.size() / 3; i++)
            roles.add(werewolf)

        /* 
            Disabled for now, as these are still WIP
            supporters.each({ if(roles.size() < users.size()) roles.add(it) })
        */

        /* Turn the remaining users into boring villagers */
        while(roles.size() < users.size())
            roles.add(villager)

        Collections.shuffle(roles)

        /* Assign the role and put everybody on a 'Ready' screen */
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

    /**
    *   Creates chats for every set of two users, as well as one group chat for everyone
    */
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

    /**
    *    @returns if the game is able to start
    */
    boolean isReady() {
        return this.getUsers().size() >= MIN_PLAYERS
    }


    /**
    *  Starts the game
    */
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
