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

    static final init() {

        Closure<Boolean> self = { User holder, User user -> holder.id == user.id }
        Closure<Boolean> notSelf = { User holder, User user -> !self(holder, user) }
        Closure<Void> kill = { users, selection -> if(selection instanceof User) selection.setDead(true) }

        register(new Action('eat', 'Who you do you want to eat?',
                { User holder, User user -> user.role?.name != 'Werewolf' },
                { User holder, User user -> user.role?.name == 'Werewolf' },
                kill)
                .displayAs('Werewolf')
        )

        register(new Action('see', 'Who do you want to see?', notSelf, self))

        register(new Action('ready', 'Are you ready?', (String[]) ['yes'], { h, u -> true }, {u, s -> void}, { User user ->
            user.role?.nightAction ?: 'sleep'
        }))

        register(new Action('lynch', 'Whose head should roll?', notSelf, { h, u -> true }, kill, { User user ->
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

    Action(String name, String message, String[] options, Closure voters = { h, u -> true }, Closure run = { u, s -> void }, Closure next = { null }) {
        this(name, message, { h, u -> false }, options, voters, run, next)
    }

    Action(String name, String message, Closure targets, Closure voters = { h, u -> true }, Closure run = { u, s -> void }, Closure next = { null }) {
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
