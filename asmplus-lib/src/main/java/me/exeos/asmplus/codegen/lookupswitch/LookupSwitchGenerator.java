package me.exeos.asmplus.codegen.lookupswitch;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LookupSwitchGenerator implements Opcodes {

    private final List<SwitchCase> cases;

    private final SwitchCase dfltCase;
    private final LabelNode switchEnd;

    public LookupSwitchGenerator(List<SwitchCase> cases) {
        this(cases, null);
    }

    private LookupSwitchGenerator(List<SwitchCase> cases, SwitchCase dfltCase) {
        this.cases = cases;
        this.dfltCase = dfltCase;
        switchEnd = new LabelNode();

        this.cases.sort(Comparator.comparingInt(o -> o.key));
    }

    /**
     * Generate a new lookup switch
     * @return Lookup switch instructions
     */
    public List<AbstractInsnNode> gen() {
        List<AbstractInsnNode> instructions = new ArrayList<>();

        instructions.add(new LookupSwitchInsnNode(dfltCase != null ? dfltCase.caseStart : switchEnd, getKeys(), getLabels()));
        for (SwitchCase switchCase : cases) {
            instructions.add(switchCase.caseStart);
            instructions.addAll(switchCase.instructions);
            instructions.add(new JumpInsnNode(GOTO, switchEnd));
        }
        if (dfltCase != null) {
            instructions.add(dfltCase.caseStart);
            instructions.addAll(dfltCase.instructions);
            instructions.add(new JumpInsnNode(GOTO, switchEnd));
        }
        instructions.add(switchEnd);

        return instructions;
    }

    protected int[] getKeys() {
        int[] keys = new int[cases.size()];

        for (int i = 0; i < cases.size(); i++) {
            keys[i] = cases.get(i).key;
        }

        return keys;
    }

    protected LabelNode[] getLabels() {
        LabelNode[] labels = new LabelNode[cases.size()];

        for (int i = 0; i < cases.size(); i++) {
            labels[i] = cases.get(i).caseStart;
        }

        return labels;
    }
}
