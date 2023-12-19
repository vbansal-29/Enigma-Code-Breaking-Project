package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author
 */
public class PermutationTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /** Check that perm has an alphabet whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                         e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                         c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                         ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                         ci, perm.invert(ei));
        }
    }

    @Test
    public void testPermuteChar() {
        Permutation p = new Permutation("(HIG)(NF) (L)",
                new Alphabet("HILFNGR"));
        assertEquals('I', p.permute('H'));
        assertEquals('G', p.permute('I'));
        assertEquals('H', p.permute('G'));
        assertEquals('F', p.permute('N'));
        assertEquals('N', p.permute('F'));
        assertEquals('L', p.permute('L'));
        assertEquals('R', p.permute('R'));
    }
    @Test
    public void testPermuteInt() {
        String cycles = "(BACD)";
        Alphabet alphabet = new Alphabet("ABCD");
        Permutation p = new Permutation(cycles, alphabet);
        assertEquals(2, p.permute(0));
    }
    @Test
    public void testInvertChar() {
        String cycles = "(BKNW) (DFG)";
        Alphabet alphabet = new Alphabet("BKNWDFG");
        Permutation p = new Permutation(cycles, alphabet);
        assertEquals('G', p.invert('D'));
    }

    @Test
    public void testInvertInt() {
        String cycles = "(BACD)";
        Alphabet alphabet = new Alphabet("ABCD");
        Permutation p = new Permutation(cycles, alphabet);
        assertEquals(1, p.invert(0));
    }

    @Test
    public void testNormalPermute() {
        Permutation p = new Permutation("(ABC) (FDE)", new Alphabet("ABCDEFG"));
        assertEquals('B', p.permute('A'));
        assertEquals('D', p.permute('F'));
        assertEquals('G', p.permute('G'));
        assertEquals('A', p.permute('C'));
        assertEquals('F', p.permute('E'));
    }

    @Test
    public void testDerangement() {
        Permutation falseP = new Permutation("(ACBD)", new Alphabet("ABCDE"));
        Permutation trueP = new Permutation("(ABCDE)", new Alphabet("ABCDE"));
        assertTrue(trueP.derangement());
        assertFalse(falseP.derangement());
    }
}
