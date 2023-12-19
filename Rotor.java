package enigma;

import static enigma.EnigmaException.*;

/** Superclass that represents a rotor in the enigma machine.
 *  @author Vishal Bansal
 */
class Rotor {
    /**Name of the Rotor. */
    private final String _name;
    /**Rotor's Permutation. */
    private Permutation _permutation;
    /**Rotor's current setting. */
    private int _curr;
    Rotor(String name, Permutation perm) {
        _name = name;
        _permutation = perm;
        _curr = 0;
    }

    /** Return my name. */
    String name() {
        return _name;
    }

    /** Return my alphabet. */
    Alphabet alphabet() {
        return _permutation.alphabet();
    }

    /** Return my permutation. */
    Permutation permutation() {
        return _permutation;
    }

    /** Return the size of my alphabet. */
    int size() {
        return _permutation.size();
    }

    /** Return true iff I have a ratchet and can move. */
    boolean rotates() {
        return false;
    }

    /** Return true iff I reflect. */
    boolean reflecting() {
        return false;
    }

    /** Return my current setting. */
    int setting() {
        return _curr;
    }

    /** Set setting() to POSN.  */
    void set(int posn) {
        _curr = permutation().wrap(posn);
    }

    /** Set setting() to character CPOSN. */
    void set(char cposn) {
        _curr = permutation().alphabet().toInt(cposn);
    }

    /** Return the conversion of P (an integer in the range 0..size()-1)
     *  according to my permutation. */
    int convertForward(int p) {
        int result = permutation().permute(permutation().wrap(p + setting()));
        result = result - setting();
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(result));
        }
        return permutation().wrap(result);
    }

    /** Return the conversion of E (an integer in the range 0..size()-1)
     *  according to the inverse of my permutation. */
    int convertBackward(int e) {
        int result = permutation().invert(permutation().wrap(e + setting()));
        result = result - setting();
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(result));
        }
        return permutation().wrap(result);
    }

    /** Returns the positions of the notches, as a string giving the letters
     *  on the ring at which they occur. */
    String notches() {
        return "";
    }

    void setNotches(String notches) { }
    /** Returns true iff I am positioned to allow the rotor to my left
     *  to advance. */
    boolean atNotch() {
        return notches().indexOf(alphabet().toChar(_curr)) != -1;
    }

    /** Advance me one position, if possible. By default, does nothing. */
    void advance() {
    }

    @Override
    public String toString() {
        return "Rotor " + _name;
    }
}
