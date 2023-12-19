package enigma;

import java.util.Collection;


/** Class that represents a complete enigma machine.
 *  @author Vishal Bansal
 */
class Machine {

    /** Alphabet. */
    private final Alphabet _alphabet;
    /** Rotors. */
    private Rotor [] _rotors;
    /** Pawls. */
    private int _pawls;
    /** AllRotors. */
    private Collection<Rotor> _allRotors;
    /** Plugboard. */
    private Permutation _plugboard;
    /** String. */
    private String _rings = "";
    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _rotors = new Rotor[numRotors];
        _pawls = pawls;
        _allRotors = allRotors;
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _rotors.length;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     *  #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     *  undefined results. */
    Rotor getRotor(int k) {
        return _rotors[k];
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    String getRings() {
        return _rings;
    }

    void setRings(String rings) {
        _rings = rings;
    }
    void reverseNotch() {
        for (int i = 1; i < _rotors.length; i++) {
            String notches = "";
            String curr = _rotors[i].notches();
            for (int j = 0; j < curr.length(); j++) {
                int original = _rotors[i].alphabet().toInt(
                        _rotors[i].notches().charAt(j));
                int subtract = _rotors[i].alphabet().toInt(
                        _rings.charAt(i - 1));
                notches += _rotors[i].alphabet().toChar(
                        _rotors[i].permutation().wrap(original + subtract));
            }
            if (curr != "") {
                _rotors[i].setNotches(notches);
            }
        }
    }
    void setNotch() {
        for (int i = 1; i < _rotors.length; i++) {
            String notches = "";
            String curr = _rotors[i].notches();
            for (int j = 0; j < curr.length(); j++) {
                int original = _rotors[i].alphabet().toInt(
                        _rotors[i].notches().charAt(j));
                int subtract = _rotors[i].alphabet().
                        toInt(_rings.charAt(i - 1));
                notches += _rotors[i].alphabet().toChar(
                        _rotors[i].permutation().wrap(original - subtract));
            }
            if (curr != "") {
                _rotors[i].setNotches(notches);
            }
        }
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        for (int i = 0; i < rotors.length; i++) {
            for (Rotor rotor: _allRotors) {
                if (rotor.name().equals(rotors[i])) {
                    _rotors[i] = rotor;
                }
            }
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() == 1) {
            int subtract = 0;
            if (_rings != "") {
                subtract = _alphabet.toInt(_rings.charAt(0));
            }
            int current = _rotors[1].permutation().wrap(_alphabet.toInt(
                    setting.charAt(0)) - subtract);
            _rotors[1].set(current);
        } else {
            for (int i = 1; i < _rotors.length; i++) {
                int subtract = 0;
                if (_rings != "") {
                    subtract = _alphabet.toInt(_rings.charAt(i - 1));
                }
                int current = _rotors[i].permutation().wrap(_alphabet.toInt(
                        setting.charAt(i - 1)) - subtract);
                _rotors[i].set(current);
            }
        }

    }

    /** Return the current plugboard's permutation. */
    Permutation plugboard() {
        return _plugboard;
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        advanceRotors();
        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /** Advance all rotors to their next position. */
    private void advanceRotors() {
        Boolean [] turned = new Boolean [_rotors.length];
        for (int i = 0; i < turned.length; i++) {
            turned[i] = false;
        }
        turned[_rotors.length - 1] = true;
        for (int i = _rotors.length - 2; i > 0; i--) {
            if (_rotors[i + 1].atNotch() && _rotors[i].notches() != "") {
                turned[i] = true;
                if (!(turned[i + 1])) {
                    turned[i + 1] = true;
                }
            }
        }
        for (int i = 0; i < turned.length; i++) {
            if (turned[i]) {
                _rotors[i].advance();
            }
        }
    }

    /** Return the result of applying the rotors to the character C (as an
     *  index in the range 0..alphabet size - 1). */

    private int applyRotors(int c) {
        for (int i = _rotors.length - 1; i >= 0; i--) {
            c = _rotors[i].convertForward(c);
        } for (int i = 1; i < _rotors.length; i++) {
            c = _rotors[i].convertBackward(c);
        }
        return c;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String result = "";

        int i = 0;
        while (i < msg.length()) {
            char curr = msg.charAt(i);
            while (curr == ' ' && i + 1 != msg.length()) {
                i++;
                curr = msg.charAt(i);
            }
            if (curr == ' ' && i + 1 == msg.length()) {
                return result;
            }
            result += _alphabet.toChar(convert(_alphabet.toInt(msg.charAt(i))));
            i++;
        }
        return result;
    }
}
