package me.exeos.asmplus.pscan;

import me.exeos.asmplus.pscan.match.AbstractInstructionMatcher;
import me.exeos.asmplus.pscan.match.matchers.MatchAny;
import me.exeos.asmplus.pscan.match.matchers.OpCodeMatcher;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class PatternScanner {

    private final ArrayList<ArrayList<AbstractInstructionMatcher>> multiMatchers = new ArrayList<>();

    public ArrayList<AbstractInsnNode> scan(MethodNode methodNode) {
        ArrayList<AbstractInsnNode> matches = new ArrayList<>();

        for (AbstractInsnNode scanStartInsn : methodNode.instructions) {
            boolean match = true;
            AbstractInsnNode currentInsn = scanStartInsn;
            matchAtInstruction:
            for (ArrayList<AbstractInstructionMatcher> matchers : multiMatchers) {
                if (currentInsn == null) {
                    match = false;
                    break;
                }

                for (AbstractInstructionMatcher matcher : matchers) {
                    if (!matcher.match(currentInsn)) {
                        match = false;
                        break matchAtInstruction;
                    }
                }
                currentInsn = currentInsn.getNext();
            }

            if (match) {
                matches.add(scanStartInsn);
            }
        }

        return matches;
    }

    public PatternScanner matchOpCode(int opcode) {
        multiMatchers.add(new ArrayList<>(List.of(new OpCodeMatcher(opcode))));
        return this;
    }

    public PatternScanner any() {
        return any(1);
    }

    public PatternScanner any(int amount) {
        for (int i = 0; i < amount; i++) {
            multiMatchers.add(new ArrayList<>(List.of(new MatchAny())));
        }
        return this;
    }

    public PatternScanner matchCustom(Predicate<AbstractInsnNode> condition) {
        multiMatchers.add(new ArrayList<>(List.of(condition::test)));
        return this;
    }

    public MultiMatchBuilder multiMatch() {
        return new MultiMatchBuilder();
    }

    public class MultiMatchBuilder {
        private final ArrayList<AbstractInstructionMatcher> matchers = new ArrayList<>();

        public MultiMatchBuilder matchOpCode(int opcode) {
            matchers.add(new OpCodeMatcher(opcode));
            return this;
        }

        public MultiMatchBuilder any() {
            return any(1);
        }

        public MultiMatchBuilder any(int amount) {
            for (int i = 0; i < amount; i++) {
                matchers.add(new MatchAny());
            }
            return this;
        }

        public MultiMatchBuilder matchCustom(Predicate<AbstractInsnNode> condition) {
            matchers.add(condition::test);
            return this;
        }

        public PatternScanner endMultiMatch() {
            multiMatchers.add(matchers);
            return PatternScanner.this;
        }
    }
}
