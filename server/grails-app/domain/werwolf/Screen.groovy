package werwolf

import werwolf.screen.ScreenDead
import werwolf.screen.ScreenSleep

abstract class Screen {

    int id
    final boolean anonymous = false

    /**
    *   Gets the next screen for a specific user   
    */
    final Screen nextScreen(User user) {
        if(user.isDead()) new ScreenDead()
        Screen next = this.next(user)
        next ?: new ScreenSleep()
    }

    abstract String getKey()
    abstract String getMessage()

    abstract String[] options();

    /**
    *   A screen always following the current
    *   ex.:    Witch healing --> Witch poisoning
    */
    protected Screen next(User user) { null }

    /**
    *   Executes the screen with a selection
    */
    abstract void run(Set<User> users, def selection);

    final Set<User> getVoters(Set<User> users, User self) {
        return users.findAll({ u -> !u.isDead() && this.isVoter(u, self) })
    }

    final Set<User> getTargets(Set<User> users, User self) {
        return users.findAll({ u -> !u.isDead() && this.isTarget(u, self) })
    }

    protected abstract boolean isTarget(User user, User self)

    protected abstract boolean isVoter(User user, User self)

    Role displayAs() { Role.findByName('Villager') }

    /**
    *   A static screen is one that does not allow any user interaction
    *   They will not require any decisions in order to be closed at the next cycle
    *   ex.:    ScreenSleep
    */
    boolean isStatic() {
        boolean noTargets = true

        users.each({ User voter ->
            noTargets = noTargets && getTargets(voter?.game?.users, voter).isEmpty()
        })

        options().length == 0 && noTargets
    }

    /**
    *   Calculates the most voted for option/target
    */
    def calculateResult() {

        Map<User, Integer> targets = [:]
        Map<String, Integer> options = [:]

        decisions.each({ Decision decision ->
            if(decision.target) targets.put(decision.target, targets.getOrDefault(decision.target, 0) + 1)
            else if(decision.selection != 'skip') options.put(decision.selection, options.getOrDefault(decision.selection, 0) + 1)
        })

        Map.Entry<User, Integer> target = targets.isEmpty() ? null : targets.max({ it.value })
        Map.Entry<String, Integer> option = options.isEmpty() ? null : options.max({ it.value })

        return target?.value > option?.value ? target?.key : option?.key
    }

    /**
    *   @returns if there is still any user input neccessary before the screen can be closed
    */
    boolean isOpen() {
        return !isStatic() && decisions?.size() < users?.size()
    }


    /**
    *   @returns if the screen can be closed
    *   returns false for static screens, as they will only be closed when the next cycle starts
    */
    boolean canClose() {
        return !this.isOpen() && !isStatic()
    }

    @Deprecated
    void checkDone() {

    }

    static belongsTo = [ game: Game ]
    static hasMany = [ decisions: Decision, users: User ]

    static constraints = {
    }

    static mapping = {
        decisions cascade: 'all-delete-orphan'
    }
}
