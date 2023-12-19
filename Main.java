package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.HashMap;

import ucb.util.CommandArgs;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Vishal Bansal
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    private HashMap<String, Rotor> rotors = new HashMap<String, Rotor>();
    /** notches. */
    private String notches = "";
    public static void main(String... args) {
        try {
            CommandArgs options =
                    new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                        + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }
    /** Open the necessary files for non-option arguments ARGS (see comment
     *  on main). */
    Main(List<String> args) {
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine M = readConfig();
        int count = 0;
        int inCount = 0;
        while (_input.hasNextLine()) {
            String curr = _input.nextLine();
            if (curr == "") {
                _output.println();
                count++;
            } else if (curr.charAt(0) == '*') {
                setUp(M, curr, inCount);
                inCount++;
                count++;
            } else {
                if (count == 0) {
                    throw error("Missing setting");
                }
                String converted = M.convert(curr);
                printMessageLine(converted);
                count++;
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            _alphabet = new Alphabet(_config.next());
            int numRotors = _config.nextInt();
            int numPawls = _config.nextInt();
            _config.nextLine();
            while (_config.hasNext()) {
                Rotor currRotor = readRotor();
                rotors.put(currRotor.name(), currRotor);
            }
            return new Machine(_alphabet, numRotors, numPawls, rotors.values());
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String name = _config.next();
            String information = _config.next();
            char type = information.charAt(0);
            String bnotches = information.substring(1);
            String cycles = "";
            String next = _config.next();
            while (next.contains(")") || next.contains("(")) {
                if (!(next.matches("\\(.+\\)"))) {
                    throw error("bad rotor description");
                }
                cycles += next;
                if (_config.hasNext("\\(.+\\)||.*\\(.*||.*\\).*")) {
                    next = _config.next();
                } else {
                    Rotor newRotor = returnRotor(type, name,
                            new Permutation(cycles, _alphabet), bnotches);
                    return newRotor;
                }
            }
            Rotor newRotor = returnRotor(type, name,
                    new Permutation(cycles, _alphabet), bnotches);
            return newRotor;
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }
    private Rotor returnRotor(char type, String name, Permutation permutation,
                              String bnotches) {
        if (type == 'N') {
            return new FixedRotor(name, permutation);
        } else if (type == 'M') {
            return new MovingRotor(name, permutation, bnotches);
        }
        return new Reflector(name, permutation);
    }
    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment and INCOUNT. */
    private void setUp(Machine M, String settings, int inCount) {
        String[] names = new String[M.numRotors()];
        Scanner curr = new Scanner(settings);
        curr.next();
        for (int i = 0; i < names.length; i++) {
            String name = curr.next();
            names[i] = name;
        }
        for (int i = 0; i < names.length; i++) {
            for (int j = 0; j < names.length; j++) {
                if (names[i].equals(names[j]) && i != j) {
                    throw error("Duplicate rotor name");
                }
            }
        }

        if (!(rotors.get(names[0]).reflecting())) {
            throw error("first rotor must be reflector");
        }

        for (int i = 0; i < names.length; i++) {
            if (!(rotors.containsKey(names[i]))) {
                throw error("Bad rotor name");
            }
        }
        String setting = curr.next();
        String ring = "";
        if (curr.hasNext() && !(curr.hasNext("(.*\\(.*|.*\\).*)"))) {
            ring = curr.next();
        }
        M.insertRotors(names);
        if (inCount != 0 && ring != "") {
            M.reverseNotch();
        }
        M.setRings(ring);
        if (ring != "") {
            M.setNotch();
        }
        int count = 0;
        for (int i = 0; i < M.numRotors(); i++) {
            if (M.getRotor(i).notches() != "") {
                count++;
            }
        }
        if (count > M.numPawls()) {
            throw error("incorrect number of parameters");
        }
        M.setRotors(setting);
        String plugboard = "";
        while (curr.hasNext()) {
            plugboard += curr.next();
        }
        M.setPlugboard(new Permutation(plugboard, _alphabet));
    }

    /** Return true iff verbose option specified. */
    static boolean verbose() {
        return _verbose;
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        int remainder = msg.length() % 5;
        int sections = (msg.length() - remainder) / 5;
        if (sections == 0) {
            _output.println(msg);
        } else {
            int current = 5;
            String result = msg.substring(0, 5);
            for (int i = 0; i < sections - 1; i++) {
                result += " " + msg.substring(current, current + 5);
                current += 5;
            }
            _output.println(result + " " + msg.substring(
                    current, current + remainder));
        }
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;
    /** True if --verbose specified. */
    private static boolean _verbose;
}
