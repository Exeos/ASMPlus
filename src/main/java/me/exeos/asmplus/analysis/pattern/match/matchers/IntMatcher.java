package me.exeos.asmplus.analysis.pattern.match.matchers;

import me.exeos.asmplus.analysis.pattern.match.AbstractInstructionMatcher;
import me.exeos.asmplus.analysis.pattern.match.NumberComparator;
import me.exeos.asmplus.utils.InsnUtil;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.Optional;

public record IntMatcher(Optional<Integer> value, NumberMatcher.MatchMode matchMode) implements AbstractInstructionMatcher {

    @Override
    public boolean match(AbstractInsnNode toMatch) {
        Optional<Integer> insnValue = InsnUtil.getIntValue(toMatch);
        if (insnValue.isEmpty()) {
            return false;
        }

        if (value.isPresent()) {
            return insnValue.filter(val -> NumberComparator.compareInt(val, value.get()) == matchMode.value).isPresent();
        }

        return true;
    }
}
