package me.exeos.asmplus.utils;

import me.exeos.asmplus.descriptor.DescriptorMember;
import me.exeos.asmplus.descriptor.DescriptorParser;
import me.exeos.asmplus.descriptor.descriptors.method.MethodDescriptor;
import me.exeos.asmplus.jar.JarArchive;
import me.exeos.asmplus.matcher.method.MethodMatchEntry;
import me.exeos.asmplus.matcher.method.MethodMatcher;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MethodUtil implements Opcodes {

    public static int getMethodReturnOpcode(MethodNode methodNode) {
        return Type.getReturnType(methodNode.desc).getOpcode(Opcodes.IRETURN);
    }

    public static InsnList endMethodByThrow() {
        InsnList insnList = new InsnList();

        insnList.add(new TypeInsnNode(Opcodes.NEW, "java/lang/RuntimeException"));
        insnList.add(new InsnNode(Opcodes.DUP));
        insnList.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "()V", false));
        insnList.add(new InsnNode(Opcodes.ATHROW));

        return insnList;
    }

    public static boolean hasAccess(MethodNode methodNode, int accessCode) {
        return AsmUtil.hasAccess(methodNode.access, accessCode);
    }

    public static int addParam(MethodNode to, DescriptorMember param) {
        return addParam(to, param, true);
    }

    /**
     * Adds a Parameter to the method, by updating its descriptor, remapping locals and increasing maxLocals
     *
     * @param to          Method to add param to
     * @param param       Param to add to method
     * @param remapLocals Should instructions loading locals be remapped
     * @return Local slot of the newly added Param
     */
    public static int addParam(MethodNode to, DescriptorMember param, boolean remapLocals) {
        MethodDescriptor newMethodDesc = DescriptorParser.parseMethodDesc(to.desc).addParam(param);
        int newParamSlot = newMethodDesc.getAbsoluteSlot(
                newMethodDesc.getParams().size() - 1,
                MethodUtil.getLocalsOffset(to));

        // loop trough each insn and update target var if it collides
        if (remapLocals) {
            remapLocals(to.instructions, newParamSlot);
        }

        to.desc = newMethodDesc.toDesc();
        to.maxLocals += param.getSlotWidth();
        return newParamSlot;
    }

    public static int getNewParamSlot(MethodNode to, DescriptorMember param) {
        MethodDescriptor newMethodDesc = DescriptorParser.parseMethodDesc(to.desc).addParam(param);

        return newMethodDesc.getAbsoluteSlot(
                newMethodDesc.getParams().size() - 1,
                MethodUtil.getLocalsOffset(to));
    }

    /**
     * Remaps indexes of locals so they don't collide with newly added params
     *
     * @param container Instructions to remap
     * @param threshold The index threshold marking the end of the method params
     */
    public static void remapLocals(InsnList container, int threshold) {
        InsnUtil.loop(container, insnNode -> {
            if (insnNode instanceof VarInsnNode varInsnNode && varInsnNode.var >= threshold) {
                varInsnNode.var++;
            } else if (insnNode instanceof IincInsnNode iincInsnNode && iincInsnNode.var >= threshold) {
                iincInsnNode.var++;
            }
        });
    }

    /**
     * Returns the start slot of the locals in a method
     *
     * @param methodNode Method node to get the offset for
     * @return The start slot of the locals in a method
     */
    public static int getLocalsOffset(MethodNode methodNode) {
        return MethodUtil.hasAccess(methodNode, ACC_STATIC) ? 0 : 1;
    }

    public static int getFirstFreeSlot(MethodNode methodNode) {
        int max = DescriptorParser.parseMethodDesc(methodNode.desc).getParamsSize() + getLocalsOffset(methodNode);

        for (AbstractInsnNode insnNode : methodNode.instructions) {
            if (insnNode instanceof VarInsnNode varInsn) {
                int size = InsnUtil.isWide(varInsn.getOpcode()) ? 2 : 1;
                max = Math.max(max, varInsn.var + size);
            }

            if (insnNode instanceof IincInsnNode) {
                max = Math.max(max, ((IincInsnNode) insnNode).var + 1);
            }
        }

        return max;
    }

    /**
     * Finds all methods targeted by invokedynamic insn in the provided methods instructions
     *
     * @param methodNode The MethodNode to scan for indy instructions
     * @return Set of owner + name + desc of targeted methods
     */
    public static MethodMatcher getInvokeDynamicTargets(MethodNode methodNode) {
        MethodMatcher targeted = new MethodMatcher();
        for (AbstractInsnNode insnNode : methodNode.instructions) {
            if (insnNode instanceof InvokeDynamicInsnNode indy) {
                for (Object bsmArg : indy.bsmArgs) {
                    if (bsmArg instanceof Handle handle) {
                        targeted.add(MethodMatchEntry.of(handle.getOwner(), handle.getName(), handle.getDesc()));
                    }
                }
            }
        }

        return targeted;
    }

    /**
     * Finds all methods targeted by invokedynamic and maps them into their owner classes
     *
     * @param jar        JarArchive containing the relevant classes
     * @param methodNode The MethodNode to scan for indy instructions
     * @return Map mapping classes and theirs methods if that method is targeted by invoke dynamic
     */
    public static Map<ClassNode, Set<MethodNode>> getInvokeDynamicTargets(JarArchive jar, MethodNode methodNode) {
        Map<ClassNode, Set<MethodNode>> targeted = new HashMap<>();
        for (AbstractInsnNode insnNode : methodNode.instructions) {
            if (insnNode instanceof InvokeDynamicInsnNode indy) {
                for (Object bsmArg : indy.bsmArgs) {
                    if (bsmArg instanceof Handle handle) {
                        jar.getClassNode(handle.getOwner(), false).ifPresent(classNode -> {
                            targeted.putIfAbsent(classNode, new HashSet<>());
                            ClassUtil.findMethod(classNode, handle.getName(), handle.getDesc()).ifPresent(targeted.get(classNode)::add);
                        });
                    }
                }
            }
        }

        return targeted;
    }

    /**
     * Scans {@code invokedynamic} call sites and excludes method signatures that should not be rewritten.
     *
     * <p>This protects lambda/indy-linked targets when their effective signature cannot be safely changed,
     * especially when:
     * <ul>
     *   <li>the indy return owner is not a class contained in this {@code jar}, or</li>
     *   <li>the bootstrap-linked method handle matches an already excluded method.</li>
     * </ul>
     *
     * <p>For each such site, both are excluded:
     * <ul>
     *   <li>the synthetic/indy target method inferred from {@code (owner, indy.name, samDescriptor)}</li>
     *   <li>the bootstrap method-handle target {@code (handle.owner, handle.name, handle.desc)}</li>
     * </ul>
     *
     * @param jar        archive being transformed
     * @param exclusions mutable matcher to expand
     */
    public static void excludeUnrewritableIndyTargets(JarArchive jar, MethodMatcher exclusions) {
        for (ClassNode classNode : jar.getClasses().values()) {
            for (MethodNode methodNode : classNode.methods) {
                for (AbstractInsnNode insnNode : methodNode.instructions) {
                    if (!(insnNode instanceof InvokeDynamicInsnNode indy)) {
                        continue;
                    }

                    DescriptorMember indyRet = DescriptorParser.parseMethodDesc(indy.desc).getReturnType();
                    if (indyRet.isPrimitive() || indyRet.isArray() || indy.bsmArgs.length < 2 || !(indy.bsmArgs[0] instanceof Type normalType) || !(indy.bsmArgs[1] instanceof Handle handle)) {
                        continue;
                    }

                    String owner = indyRet.getValue();
                    String name = indy.name;
                    String desc = normalType.getDescriptor();

                    if (!jar.getClasses().containsKey(owner)
                            || exclusions.match(
                            MethodMatchEntry.of(handle.getOwner(), handle.getName(), handle.getDesc())
                    )
                    ) {
                        exclusions.add(MethodMatchEntry.of(owner, name, desc));
                        exclusions.add(MethodMatchEntry.of(handle.getOwner(), handle.getName(), handle.getDesc()));
                    }
                }
            }
        }
    }

    public static boolean isSpecial(MethodNode methodNode) {
        return methodNode.name.equals("<init>") || methodNode.name.equals("<clinit>");
    }

    public static void removeAllInsn(MethodNode methodNode) {
        for (AbstractInsnNode insnNode : methodNode.instructions) {
            methodNode.instructions.remove(insnNode);
        }
    }

    public static void remapTryCatchBlock(MethodNode methodNode, Map<LabelNode, LabelNode> labelMap) {
        for (TryCatchBlockNode tryCatchBlock : methodNode.tryCatchBlocks) {
            tryCatchBlock.start = labelMap.get(tryCatchBlock.start);
            tryCatchBlock.end = labelMap.get(tryCatchBlock.end);
            tryCatchBlock.handler = labelMap.get(tryCatchBlock.handler);
        }
    }
}
