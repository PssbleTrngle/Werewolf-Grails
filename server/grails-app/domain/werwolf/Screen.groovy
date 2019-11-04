package werwolf

import werwolf.screen.ScreenDead
import werwolf.screen.ScreenSleep

abstract class Vote {

    int id
    final boolean anonymous = false

    Vote(String message, String result = null) {
        this.message = message
        this.result = result
    }

    final Vote nextScreen(User user) {
        if(user.isDead()) new ScreenDead()
        Vote next = this.next(user)
        next ?: new ScreenSleep()
    }

    final String message
    final String result

    abstract String getKey()

    abstract String[] options();

    private Vote next(User user) { null }

    abstract void run(User user, def selection);

    Set<User> getVoters(Set<User> users, User holder) {
        return users.findAll({ u -> !u.isDead() && this.isVoter(holder, u) })
    }

    Set<User> getTargets(Set<User> users, User holder) {
        return users.findAll({ u -> !u.isDead() && this.isTarget(holder, u) })
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
        result nullable: true
        result blank: false
    }

    static mapping = {
        decisions cascade: 'all-delete-orphan'
    }
}
