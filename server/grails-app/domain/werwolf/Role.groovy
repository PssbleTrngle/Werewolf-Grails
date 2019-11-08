package werwolf

abstract class Role {

    /** *
     * @return boolean: true if death should be canceled
     */
    boolean onDeath(User user) { false }

    int id
    String name

    Screen nightScreen() { null }

    abstract boolean triggerWin(Game game);
    boolean hasWon(Game game) { triggerWin(game) }

}
