package enigma;

import static enigma.EnigmaException.*;
/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Vishal Bansal
 */
class Permutation {
    /** Alphabet of this permutation. */
    private Alphabet _alphabet;
    /** cycles. */
    private String _cycle;

    Permutation(String cycles, Alphabet alphabet) {
        _cycle = cycles;
        _alphabet = alphabet;
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        _cycle = _cycle + " " + cycle;
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        return _alphabet.toInt(permute(_alphabet.toChar(wrap(p))));
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        return _alphabet.toInt(invert(_alphabet.toChar(wrap(c))));
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        int cycleIndex = _cycle.indexOf(p);
        if (_cycle.indexOf(p) != -1) {
            if ((_cycle.charAt(cycleIndex + 1) == ')')) {
                int frontPIndex = _cycle.lastIndexOf("(", cycleIndex + 1);
                return _alphabet.toChar(_alphabet.toInt(
                        _cycle.charAt(frontPIndex + 1)));
            } else {
                return _alphabet.toChar(_alphabet.toInt(
                        _cycle.charAt(cycleIndex + 1)));
            }
        }
        return p;
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        int cycleIndex = _cycle.indexOf(c);
        if (_cycle.indexOf(c) != -1) {
            if ((_cycle.charAt(cycleIndex - 1) == '(')) {
                String remaining = _cycle.substring(cycleIndex + 1,
                        _cycle.length());
                return _cycle.charAt(remaining.indexOf(')') + cycleIndex);
            } else {
                return _cycle.charAt(cycleIndex - 1);
            }
        }
        return c;
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        String cyclesString = _cycle;
        for (int i = 0; i < _alphabet.size(); i++) {
            if (_cycle.indexOf(_alphabet.toChar(i)) == -1) {
                return false;
            }
            int pIndex = cyclesString.indexOf('(');
            if (_cycle.charAt(pIndex + 2) == ')') {
                return false;
            }
        }
        return true;
    }
}
