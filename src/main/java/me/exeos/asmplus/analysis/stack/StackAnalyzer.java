package me.exeos.asmplus.analysis.stack;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StackAnalyzer {

    public static Map<String, List<InsnFrame>> groupFrames(String owner, MethodNode methodNode) {
        Map<String, List<InsnFrame>> grouped = new HashMap<>();

        Analyzer<BasicValue> analyzer = new Analyzer<>(new BasicInterpreter());
        Frame<BasicValue>[] frames;
        try {
            frames = analyzer.analyze(owner, methodNode);
        } catch (AnalyzerException e) {
            return grouped;
        }

        for (int i = 0; i < frames.length; i++) {
            AbstractInsnNode insnNode = methodNode.instructions.get(i);
            Frame<BasicValue> frame = frames[i];

            grouped.computeIfAbsent(frameToString(frame), _ -> new ArrayList<>()).add(new InsnFrame(insnNode, frame));

        }

        return grouped;
    }

    public static String frameToString(Frame<BasicValue> frame) {
        if (frame == null) {
            return null;
        }

        StringBuilder frameStr = new StringBuilder();
        frameStr.append(frame.getStackSize());
        frameStr.append(".");

        for (int i = 0; i < frame.getStackSize(); i++) {
            frameStr.append(frame.getStack(i).hashCode());
        }

        return frameStr.toString();
    }

    public record InsnFrame(AbstractInsnNode insnNode, Frame<BasicValue> frame) {
    }
}
