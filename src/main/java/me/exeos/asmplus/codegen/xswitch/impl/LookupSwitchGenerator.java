package me.exeos.asmplus.codegen.xswitch.impl;

import me.exeos.asmplus.codegen.xswitch.SwitchCase;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Comparator;
import java.util.List;


public class LookupSwitchGenerator {

    public static InsnList gen(List<SwitchCase> cases) {
        return gen(cases, null);
    }

    public static InsnList gen(List<SwitchCase> cases, SwitchCase dfltCase) {
        return gen(cases, dfltCase, true);
    }

    /**
     * Generate a new lookup switch
     * @return Lookup switch instructions
     */
    public static InsnList gen(List<SwitchCase> cases, SwitchCase dfltCase, boolean gotoSwitchEnd) {
        cases.sort(Comparator.comparingInt(o -> o.key));

        InsnList switchInsns = new InsnList();
        LabelNode switchEnd = new LabelNode();

        switchInsns.add(new LookupSwitchInsnNode(dfltCase != null ? dfltCase.caseStart : switchEnd, getKeys(cases), getLabels(cases)));
        for (SwitchCase switchCase : cases) {
            switchInsns.add(getCaseInsn(switchCase, gotoSwitchEnd, switchEnd));
        }
        if (dfltCase != null) {
            switchInsns.add(getCaseInsn(dfltCase, gotoSwitchEnd, switchEnd));
        }

        if (gotoSwitchEnd || dfltCase == null) {
            switchInsns.add(switchEnd);
        }

        return switchInsns;
    }

    private static int[] getKeys(List<SwitchCase> cases) {
        int[] keys = new int[cases.size()];

        for (int i = 0; i < cases.size(); i++) {
            keys[i] = cases.get(i).key;
        }

        return keys;
    }

    public static LabelNode[] getLabels(List<SwitchCase> cases) {
        LabelNode[] labels = new LabelNode[cases.size()];

        for (int i = 0; i < cases.size(); i++) {
            labels[i] = cases.get(i).caseStart;
        }

        return labels;
    }

    public static InsnList getCaseInsn(SwitchCase switchCase, boolean gotoSwitchEnd, LabelNode switchEnd) {
        InsnList caseInsn = new InsnList();

        caseInsn.add(switchCase.caseStart);
        caseInsn.add(switchCase.instructions);
        if (gotoSwitchEnd) {
            caseInsn.add(new JumpInsnNode(Opcodes.GOTO, switchEnd));
        }

        return caseInsn;
    }
}
