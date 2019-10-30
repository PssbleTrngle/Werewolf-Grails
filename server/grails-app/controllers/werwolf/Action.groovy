package werwolf

class Action {

    private static Map<String, Action> ACTIONS = [:]
    final Closure<String> next
    final Closure<Void> run
    private final Closure<Boolean> targets
    private final Closure<Boolean> voters
    private String displayAs = 'Villager'

    Role displayAs() {
        return Role.findByName(this.displayAs)
    }

    Action displayAs(String role) {
        this.displayAs = role
        return this
    }

    static get(String action) {
        return ACTIONS.get(action)
    }

    private static register(Action action) {
        assert !ACTIONS.containsKey(action)
        ACTIONS.put(action.name, action)
    }

    private static final Closure<Boolean> ALL = { h, u -> true }
    private static final Closure<Boolean> NONE = { h, u -> false }

    static final init() {

        final Closure<Boolean> SELF = { h, u -> h.id == u.id }
        final Closure<Boolean> OTHERS = { h, u -> !SELF(h, u) }
        final Closure<Boolean> OWN_ROLE = { h, u -> h.role.id == u.role.id }
        final Closure<Boolean> OTHER_ROLE = { h, u -> !OWN_ROLE(h, u) }

        final Closure<Void> KILL = { users, selection -> if(selection instanceof User) selection.kill() }

        register(new Action('eat', 'Who you do you want to eat?',
                OTHER_ROLE,
                OWN_ROLE,
                { user, selection ->
                    KILL(user, selection)

                })
                .displayAs('Werewolf')
        )

        register(new Action('see', 'Who do you want to see?', OTHERS, SELF))

        register(new Action('hunt', 'Take someone with you', OTHERS, SELF, KILL))

        register(new Action('ready', 'Are you ready?', (String[]) ['yes'], ALL, { u, s -> void}))
        register(new Action('lynch', 'Whose head should roll?', OTHERS, ALL, KILL))

        register(new Action('sleep', 'You are sleeping', NONE, SELF))
        register(new Action('dead', 'You are dead', NONE, SELF))

        register(new Action('won', 'You lost', NONE, SELF))
        register(new Action('lost', 'You won', NONE, SELF))

    }

    Set<User> getVoters(Set<User> users, User holder) {
        return users.findAll({ u -> !u.isDead() && this.voters(holder, u) })
    }

    Set<User> getTargets(Set<User> users, User holder) {
        return users.findAll({ u -> !u.isDead() && this.targets(holder, u) })
    }

    final String message
    final String name
    final String[] options

    Action(String name, String message, String[] options, Closure voters = ALL, Closure run = { u, s -> void }, Closure next = { null }) {
        this(name, message, NONE, options, voters, run, next)
    }

    Action(String name, String message, Closure targets, Closure voters = ALL, Closure run = { u, s -> void }, Closure next = { null }) {
        this(name, message, targets, new String[0], voters, run, next)
    }

    Action(String name, String message, Closure targets, String[] options, Closure voters, Closure run = { u, s -> void }, Closure next = { null }) {
        this.run = run
        this.next = next
        this.targets = targets
        this.voters = voters
        this.name = name
        this.options = options
        this.message = message
    }

    Action nextAction(User user) {
        if(user.isDead()) return get('dead')

        String next = this.next(user)
        if(user.game?.isIsNight() && !next) next = 'sleep'
        get(next)
    }

}
