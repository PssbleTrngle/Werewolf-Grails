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

    private static final Closure<Boolean> SELF = { User holder, User user -> holder.id == user.id }
    private static final Closure<Boolean> OTHERS = { User holder, User user -> !SELF(holder, user) }
    private static final Closure<Boolean> ALL = { h, u -> true }
    private static final Closure<Boolean> NONE = { h, u -> false }

    private static final Closure<Void> KILL = { users, selection -> if(selection instanceof User) selection.setDead(true) }

    static final init() {

        register(new Action('eat', 'Who you do you want to eat?',
                { User holder, User user -> user.role?.name != 'Werewolf' },
                { User holder, User user -> user.role?.name == 'Werewolf' },
                KILL)
                .displayAs('Werewolf')
        )

        register(new Action('see', 'Who do you want to see?', OTHERS, SELF))

        register(new Action('ready', 'Are you ready?', (String[]) ['yes'], ALL, { u, s -> void}, { User user ->
            user.role?.nightAction ?: 'sleep'
        }))

        register(new Action('lynch', 'Whose head should roll?', OTHERS, ALL, KILL, { User user ->
            user.role?.nightAction ?: 'sleep'
        }))

        register(new Action('sleep', 'You are sleeping'))

        register(new Action('dead', 'You are dead'))

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
        String next = this.next(user)
        if(!next) next = 'sleep'
        get(next)
    }

}
