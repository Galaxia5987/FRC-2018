package auxiliary;

/** Interface that must be implemented for a subsystem to be monitered by Watch_Doge.
 */
public interface Watch_Dogeable {
    /**
     * When Watch_Doge smells something wrong, he/she/zhe (we're inclusive here) needs a way to express "such disgusting wow"
     * (e.g. disable the the subsystem. This method must be overloaded to complete any necessary disabling
     * tasks (disable motors, etc.).
     */
	public void bork();

    /**
     * Method Watch_Doge calls to "revive" (re-enable) a subsystem after being borking at one in a previous iteration.
     * Must be overloaded to run any tasks necessary to begin life once again.
     */
	public void necromancy();

    /**
     * Even though Watch_Doge may seem aggressive with a bork capable of disabling subsystems, he/she/zhe (we are inclusive
     * here) will always gently ask the subsystem if it is willing to go back to the realm of the living.
     * Must be overloaded to return true to tell Watch_Doge if he/she/zhe can perform necromancy, false if the subsystem
     * wants to remain dead for another iteration.
     *
     * @return boolean indicating if subsystem is ready to be re-enabled via necromancy (true is ready)
     */
	public boolean wakeMeUp();

    /**
     * Watch_Doge also can poke the subsystem with a stick to check if it is alive. Overload to return if the
     * subsystem is currently enabled or disabled.
     *
     * @return boolean indicating if subsystem is enabled or disabled (true is enabled)
     */
	public boolean ded();
}