package werwolf

import werwolf.screen.ScreenDead
import werwolf.screen.ScreenSleep

abstract class Screen {

    int id
    final boolean anonymous = false

    final Screen nextScreen(User user) {
        if(user.isDead()) new ScreenDead()
        Screen next = this.next(user)
        next ?: new ScreenSleep()
    }

    abstract String getKey()
    abstract String getMessage()

    abstract String[] options();

    protected Screen next(User user) { null }

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

    boolean isStatic() {
        boolean noTargets = true

        users.each({ User voter ->
            noTargets = noTargets && getTargets(voter?.game?.users, voter).isEmpty()
        })

        options().length == 0 && noTargets
    }

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

    boolean isOpen() {
        return !isStatic() && decisions?.size() < users?.size()
    }

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
