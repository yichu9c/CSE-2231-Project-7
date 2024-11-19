import components.sequence.Sequence;
import components.statement.Statement;
import components.statement.StatementSecondary;
import components.tree.Tree;
import components.tree.Tree1;
import components.utilities.Tokenizer;

/**
 * {@code Statement} represented as a {@code Tree<StatementLabel>} with
 * implementations of primary methods.
 *
 * @convention [$this.rep is a valid representation of a Statement]
 * @correspondence this = $this.rep
 *
 * @author Shyam Sai Bethina and Yihone Chu
 *
 */
public class Statement2 extends StatementSecondary {

    /*
     * Private members --------------------------------------------------------
     */

    /**
     * Label class for the tree representation.
     */
    private static final class StatementLabel {

        /**
         * Statement kind.
         */
        private Kind kind;

        /**
         * IF/IF_ELSE/WHILE statement condition.
         */
        private Condition condition;

        /**
         * CALL instruction name.
         */
        private String instruction;

        /**
         * Constructor for BLOCK.
         *
         * @param k
         *            the kind of statement
         */
        private StatementLabel(Kind k) {
            assert k == Kind.BLOCK : "Violation of: k = BLOCK";
            this.kind = k;
        }

        /**
         * Constructor for IF, IF_ELSE, WHILE.
         *
         * @param k
         *            the kind of statement
         * @param c
         *            the statement condition
         */
        private StatementLabel(Kind k, Condition c) {
            assert k == Kind.IF || k == Kind.IF_ELSE || k == Kind.WHILE : ""
                    + "Violation of: k = IF or k = IF_ELSE or k = WHILE";
            this.kind = k;
            this.condition = c;
        }

        /**
         * Constructor for CALL.
         *
         * @param k
         *            the kind of statement
         * @param i
         *            the instruction name
         */
        private StatementLabel(Kind k, String i) {
            assert k == Kind.CALL : "Violation of: k = CALL";
            assert i != null : "Violation of: i is not null";
            assert Tokenizer
                    .isIdentifier(i) : "Violation of: i is an IDENTIFIER";
            this.kind = k;
            this.instruction = i;
        }

        @Override
        public String toString() {
            String condition = "?", instruction = "?";
            if ((this.kind == Kind.IF) || (this.kind == Kind.IF_ELSE)
                    || (this.kind == Kind.WHILE)) {
                condition = this.condition.toString();
            } else if (this.kind == Kind.CALL) {
                instruction = this.instruction;
            }
            return "(" + this.kind + "," + condition + "," + instruction + ")";
        }

    }

    /**
     * The tree representation field.
     */
    private Tree<StatementLabel> rep;

    /**
     * Creator of initial representation.
     */
    private void createNewRep() {
        /*
         * Creates a new representation by setting the root of the rep to
         * Kind.Block, and the children are empty Trees of statement labels.
         */
        this.rep = new Tree1<StatementLabel>();
        Sequence<Tree<StatementLabel>> children = this.rep.newSequenceOfTree();
        StatementLabel s = new StatementLabel(Kind.BLOCK);
        this.rep.assemble(s, children);
    }

    /*
     * Constructors -----------------------------------------------------------
     */

    /**
     * No-argument constructor.
     */
    public Statement2() {
        /*
         * Creates a new representation by calling .createNewRep()
         */
        this.createNewRep();
    }

    /*
     * Standard methods -------------------------------------------------------
     */

    @Override
    public final Statement2 newInstance() {
        try {
            return this.getClass().getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(
                    "Cannot construct object of type " + this.getClass());
        }
    }

    @Override
    public final void clear() {
        this.createNewRep();
    }

    @Override
    public final void transferFrom(Statement source) {
        assert source != null : "Violation of: source is not null";
        assert source != this : "Violation of: source is not this";
        assert source instanceof Statement2 : ""
                + "Violation of: source is of dynamic type Statement2";
        /*
         * This cast cannot fail since the assert above would have stopped
         * execution in that case: source must be of dynamic type Statement2.
         */
        Statement2 localSource = (Statement2) source;
        this.rep = localSource.rep;
        localSource.createNewRep();
    }

    /*
     * Kernel methods ---------------------------------------------------------
     */

    @Override
    public final Kind kind() {
        /*
         * Returns the kind of this' representation's root.
         */
        return this.rep.root().kind;
    }

    @Override
    public final void addToBlock(int pos, Statement s) {
        assert s != null : "Violation of: s is not null";
        assert s instanceof Statement2 : "Violation of: s is a Statement2";
        assert this.kind() == Kind.BLOCK : ""
                + "Violation of: [this is a BLOCK statement]";
        assert 0 <= pos : "Violation of: 0 <= pos";
        assert pos <= this.lengthOfBlock() : ""
                + "Violation of: pos <= [length of this BLOCK]";
        assert s.kind() != Kind.BLOCK : "Violation of: [s is not a BLOCK statement]";
        //use this to access s sequence
        Statement2 locals = (Statement2) s;

        /*
         * Creates an empty sequence of trees of StatementLabels, and
         * disassembles the representation.
         */
        Sequence<Tree<StatementLabel>> children = this.rep.newSequenceOfTree();
        StatementLabel label = this.rep.disassemble(children);

        /*
         * Add the representation of locals to the position from the parameters.
         */
        children.add(pos, locals.rep);

        /*
         * Gets rid of the locals, and reassembles the representation with the
         * new block.
         */
        locals.createNewRep(); //gets rid of locals
        this.rep.assemble(label, children);
    }

    @Override
    public final Statement removeFromBlock(int pos) {
        assert 0 <= pos : "Violation of: 0 <= pos";
        assert pos < this.lengthOfBlock() : ""
                + "Violation of: pos < [length of this BLOCK]";
        assert this.kind() == Kind.BLOCK : ""
                + "Violation of: [this is a BLOCK statement]";
        /*
         * The following call to Statement newInstance method is a violation of
         * the kernel purity rule. However, there is no way to avoid it and it
         * is safe because the convention clearly holds at this point in the
         * code.
         */
        Statement2 temp = new Statement2();

        /*
         * Removes the subtree's representation at the position from the
         * parameter list, and returns it.
         */
        temp.rep = this.rep.removeSubtree(pos);

        return temp;
    }

    @Override
    public final int lengthOfBlock() {
        assert this.kind() == Kind.BLOCK : ""
                + "Violation of: [this is a BLOCK statement]";

        /*
         * Returns the number of subtrees in the representation.
         */
        return this.rep.numberOfSubtrees();
    }

    @Override
    public final void assembleIf(Condition c, Statement s) {
        assert c != null : "Violation of: c is not null";
        assert s != null : "Violation of: s is not null";
        assert s instanceof Statement2 : "Violation of: s is a Statement2";
        assert s.kind() == Kind.BLOCK : ""
                + "Violation of: [s is a BLOCK statement]";

        Statement2 locals = (Statement2) s;
        StatementLabel newLabel = new StatementLabel(Kind.IF, c);

        /*
         * Creates an empty sequence of trees of StatementLabels, and
         * disassembles the representation.
         */
        Sequence<Tree<StatementLabel>> children = this.rep.newSequenceOfTree();

        /*
         * Add the representation of locals to the position from the parameters.
         */
        children.add(0, locals.rep);

        /*
         * Gets rid of the locals, and reassembles the representation with the
         * new if block.
         */
        this.rep.assemble(newLabel, children);
        locals.createNewRep(); // clears s
    }

    @Override
    public final Condition disassembleIf(Statement s) {
        assert s != null : "Violation of: s is not null";
        assert s instanceof Statement2 : "Violation of: s is a Statement2";
        assert this.kind() == Kind.IF : "Violation of: [s is an IF statement]";

        Statement2 localS = (Statement2) s;

        /*
         * Creates an empty sequence of trees of StatementLabels, and
         * disassembles the representation.
         */
        Sequence<Tree<StatementLabel>> children = this.rep.newSequenceOfTree();
        StatementLabel label = this.rep.disassemble(children);

        /*
         * Removes the first block from the representation.
         */
        localS.rep = children.remove(0);

        this.createNewRep(); // clears this

        /*
         * Returns the condition of the label.
         */
        return label.condition;
    }

    @Override
    public final void assembleIfElse(Condition c, Statement s1, Statement s2) {
        assert c != null : "Violation of: c is not null";
        assert s1 != null : "Violation of: s1 is not null";
        assert s2 != null : "Violation of: s2 is not null";
        assert s1 instanceof Statement2 : "Violation of: s1 is a Statement2";
        assert s2 instanceof Statement2 : "Violation of: s2 is a Statement2";
        assert s1
                .kind() == Kind.BLOCK : "Violation of: [s1 is a BLOCK statement]";
        assert s2
                .kind() == Kind.BLOCK : "Violation of: [s2 is a BLOCK statement]";

        Statement2 locals1 = (Statement2) s1;
        Statement2 locals2 = (Statement2) s2;

        /*
         * Creates an empty sequence of trees of StatementLabels, and
         * disassembles the representation.
         */
        StatementLabel newLabel = new StatementLabel(Kind.IF_ELSE, c);
        Sequence<Tree<StatementLabel>> children = this.rep.newSequenceOfTree();

        /*
         * Add the representation of locals to the positions from the
         * parameters.
         */
        children.add(0, locals1.rep);
        children.add(1, locals2.rep);

        /*
         * Gets rid of the locals, and reassembles the representation with the
         * new if else block.
         */
        this.rep.assemble(newLabel, children);

        //do this to get rid of the local statements
        locals1.createNewRep();
        locals2.createNewRep();
    }

    @Override
    public final Condition disassembleIfElse(Statement s1, Statement s2) {
        assert s1 != null : "Violation of: s1 is not null";
        assert s2 != null : "Violation of: s1 is not null";
        assert s1 instanceof Statement2 : "Violation of: s1 is a Statement2";
        assert s2 instanceof Statement2 : "Violation of: s2 is a Statement2";
        assert this
                .kind() == Kind.IF_ELSE : "Violation of: [s is an IF_ELSE statement]";

        Statement2 localS1 = (Statement2) s1;
        Statement2 localS2 = (Statement2) s2;

        /*
         * Creates an empty sequence of trees of StatementLabels, and
         * disassembles the representation.
         */
        Sequence<Tree<StatementLabel>> children = this.rep.newSequenceOfTree();
        StatementLabel label = this.rep.disassemble(children);

        /*
         * Removes the first block from the representation.
         */
        localS1.rep = children.remove(0);
        localS2.rep = children.remove(0);

        this.createNewRep(); // clears this

        /*
         * Returns the condition of the label.
         */
        return label.condition;
    }

    @Override
    public final void assembleWhile(Condition c, Statement s) {
        assert c != null : "Violation of: c is not null";
        assert s != null : "Violation of: s is not null";
        assert s instanceof Statement2 : "Violation of: s is a Statement2";
        assert s.kind() == Kind.BLOCK : "Violation of: [s is a BLOCK statement]";

        Statement2 localS = (Statement2) s;
        StatementLabel newLabel = new StatementLabel(Kind.WHILE, c);

        /*
         * Creates an empty sequence of trees of StatementLabels, and
         * disassembles the representation.
         */
        Sequence<Tree<StatementLabel>> children = this.rep.newSequenceOfTree();

        /*
         * Add the representation of locals to the positions from the
         * parameters.
         */
        children.add(0, localS.rep);

        /*
         * Gets rid of the locals, and reassembles the representation with the
         * new while block.
         */
        this.rep.assemble(newLabel, children);

        localS.createNewRep(); // clears

    }

    @Override
    public final Condition disassembleWhile(Statement s) {
        assert s != null : "Violation of: s is not null";
        assert s instanceof Statement2 : "Violation of: s is a Statement2";
        assert this
                .kind() == Kind.WHILE : "Violation of: [s is a WHILE statement]";

        Statement2 localS = (Statement2) s;

        /*
         * Creates an empty sequence of trees of StatementLabels, and
         * disassembles the representation.
         */
        Sequence<Tree<StatementLabel>> children = this.rep.newSequenceOfTree();
        StatementLabel label = this.rep.disassemble(children);

        /*
         * Removes the first block from the representation.
         */
        localS.rep = children.remove(0);

        this.createNewRep(); // clears this

        /*
         * Returns the condition of the label.
         */
        return label.condition;
    }

    @Override
    public final void assembleCall(String inst) {
        assert inst != null : "Violation of: inst is not null";
        assert Tokenizer.isIdentifier(inst) : ""
                + "Violation of: inst is a valid IDENTIFIER";

        StatementLabel newLabel = new StatementLabel(Kind.CALL, inst);
        /*
         * Creates an empty sequence of trees of StatementLabels, and
         * disassembles the representation.
         */
        Sequence<Tree<StatementLabel>> children = this.rep.newSequenceOfTree();

        /*
         * Reassembles the representation with the new call.
         */
        this.rep.assemble(newLabel, children);
    }

    @Override
    public final String disassembleCall() {
        assert this
                .kind() == Kind.CALL : "Violation of: [s is a CALL statement]";

        /*
         * Creates an empty sequence of trees of StatementLabels, and
         * disassembles the representation.
         */
        Sequence<Tree<StatementLabel>> children = this.rep.newSequenceOfTree();
        StatementLabel label = this.rep.disassemble(children);

        this.createNewRep(); //clears this

        /*
         * Returns the condition of the label.
         */
        return label.instruction;

    }

}
