package me.exeos.asmplus.codegen.xswitch.impl;

import me.exeos.asmplus.codegen.xswitch.SwitchCase;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TableSwitchGenerator {

    public static InsnList gen(List<SwitchCase> cases, int min, int max) {
        return gen(cases, min, max, null);
    }

    public static InsnList gen(List<SwitchCase> cases, int min, int max, SwitchCase defaultCase) {
        return gen(cases, min, max, defaultCase, true, new LabelNode());
    }

    public static InsnList gen(List<SwitchCase> cases, int min, int max, SwitchCase defaultCase, boolean gotoSwitchEnd, LabelNode switchEnd) {
        InsnList switchInsn = new InsnList();

        switchInsn.add(new TableSwitchInsnNode(min, max, defaultCase == null ? switchEnd : defaultCase.caseStart, LookupSwitchGenerator.getLabels(cases)));
        Set<SwitchCase> seen = new HashSet<>();
        for (SwitchCase switchCase : cases) {
            if (!seen.contains(switchCase)) {
                switchInsn.add(LookupSwitchGenerator.getCaseInsn(switchCase, gotoSwitchEnd, switchEnd));
                seen.add(switchCase);
            }
        }

        if (defaultCase != null) {
            switchInsn.add(LookupSwitchGenerator.getCaseInsn(defaultCase, gotoSwitchEnd, switchEnd));
        }

        if (defaultCase == null || gotoSwitchEnd) {
            switchInsn.add(switchEnd);
        }

        return switchInsn;
    }
}
