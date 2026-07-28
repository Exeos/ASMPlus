package me.exeos.asmplus.remapper;

import me.exeos.asmplus.descriptor.DescriptorMember;
import me.exeos.asmplus.descriptor.DescriptorParser;
import me.exeos.asmplus.descriptor.descriptors.method.MethodDescriptor;
import me.exeos.asmplus.jar.JarArchive;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Remaps class names across an entire {@link JarArchive} using a provided name mapping.
 *
 * <p>Handles remapping of:
 * <ul>
 *   <li>Class names, superclasses, and interfaces</li>
 *   <li>Field and method descriptors</li>
 *   <li>Instruction operands (field/method/type/LDC/invokedynamic)</li>
 *   <li>Annotations (visible, invisible, type)</li>
 *   <li>Bootstrap method handles and arguments</li>
 * </ul>
 *
 * <p>The mapping uses internal JVM names (e.g. {@code com/example/Foo}).
 */

public class ClassRemapper {

    private final Map<String, String> mapping;

    /**
     * @param mapping a map from old internal class name to new internal class name
     */
    public ClassRemapper(Map<String, String> mapping) {
        this.mapping = mapping;
    }


    /**
     * Applies the remapping to all classes in the given archive.
     * Also updates the archive's internal class map so keys reflect the new names.
     *
     * @param archive the jar archive to remap in-place
     */
    public void remap(JarArchive archive) {
        rebuildArchiveMap(archive);
        archive.getClasses().values().forEach(this::remapClassNode);
    }

    /**
     * Class level remapping
     *
     * @param classNode ClassNode to be remapped
     */
    private void remapClassNode(ClassNode classNode) {
        classNode.name = getMapped(classNode.name);
        classNode.superName = getMapped(classNode.superName);
        classNode.outerClass = getMapped(classNode.outerClass);
        classNode.nestHostClass = getMapped(classNode.nestHostClass);
        classNode.interfaces.replaceAll(this::getMapped);
        if (classNode.nestMembers != null) {
            classNode.nestMembers.replaceAll(this::getMapped);
        }
        if (classNode.permittedSubclasses != null) {
            classNode.permittedSubclasses.replaceAll(this::getMapped);
        }
        classNode.signature = null;

        remapInnerClasses(classNode.innerClasses);
        remapAnnotationList(classNode.visibleAnnotations);
        remapAnnotationList(classNode.invisibleAnnotations);
        remapTypeAnnotationList(classNode.visibleTypeAnnotations);
        remapTypeAnnotationList(classNode.invisibleTypeAnnotations);

        classNode.fields.forEach(this::remapFieldNode);
        classNode.methods.forEach(this::remapMethodNode);
    }

    private void rebuildArchiveMap(JarArchive archive) {
        Map<String, ClassNode> newMap = new HashMap<>();
        for (Map.Entry<String, ClassNode> entry : archive.getClasses().entrySet()) {
            newMap.put(mapping.getOrDefault(entry.getKey(), entry.getKey()), entry.getValue());
        }

        archive.setClasses(newMap);
    }

    private void remapInnerClasses(List<InnerClassNode> innerClasses) {
        for (InnerClassNode innerClassNode : innerClasses) {
            innerClassNode.name = getMapped(innerClassNode.name);
            innerClassNode.outerName = getMapped(innerClassNode.outerName);
            String innerNameMapped = getMapped(innerClassNode.innerName.replace(".", "/"));
            innerClassNode.innerName = innerNameMapped.substring(Math.max(0, innerNameMapped.lastIndexOf("/")));
        }
    }

    private void remapAnnotationList(List<AnnotationNode> annotations) {
        if (annotations != null) {
            annotations.replaceAll(this::remapAnnotation);
        }
    }

    private void remapTypeAnnotationList(List<TypeAnnotationNode> annotations) {
        if (annotations != null) {
            annotations.replaceAll(this::remapTypeAnnotation);
        }
    }

    private void remapFieldNode(FieldNode target) {
        target.desc = remapDescMember(
                DescriptorParser.parseFieldDesc(target.desc)
        ).toDesc();
    }

    private void remapMethodNode(MethodNode target) {
        target.desc = remapMethodDescStr(DescriptorParser.parseMethodDesc(target.desc));

        for (AbstractInsnNode insnNode : target.instructions) {
            switch (insnNode) {
                case FieldInsnNode fieldInsnNode -> remapFieldInsnNode(fieldInsnNode);
                case MethodInsnNode methodInsnNode -> remapMethodInsnNode(methodInsnNode);
                case TypeInsnNode typeInsnNode ->
                        typeInsnNode.desc = remapDescMember(DescriptorParser.parseType(typeInsnNode.desc)).toType();
                case LdcInsnNode ldcInsnNode -> remapLdcInsnNode(ldcInsnNode);
                case InvokeDynamicInsnNode indy -> {
                    indy.desc = remapMethodDescStr(DescriptorParser.parseMethodDesc(indy.desc));
                    indy.bsm = remapHandle(indy.bsm);

                    for (int i = 0; i < indy.bsmArgs.length; i++) {
                        switch (indy.bsmArgs[i]) {
                            case Handle handle -> indy.bsmArgs[i] = remapHandle(handle);
                            case Type type -> indy.bsmArgs[i] = remapType(type);
                            default -> System.out.println("Ignored BSM-Arg: " + indy.bsmArgs[i].getClass().getSimpleName());
                        }
                    }
                }
                default -> {
                }
            }
        }
    }

    private TypeAnnotationNode remapTypeAnnotation(TypeAnnotationNode annotationNode) {
        annotationNode.desc = remapDescMember(DescriptorParser.parseMember(annotationNode.desc)).toDesc();
        if (annotationNode.values != null) {
            annotationNode.values.replaceAll(this::remapAnnotationValue);
        }

        return annotationNode;
    }

    private AnnotationNode remapAnnotation(AnnotationNode annotationNode) {
        annotationNode.desc = remapDescMember(DescriptorParser.parseMember(annotationNode.desc)).toDesc();
        if (annotationNode.values != null) {
            annotationNode.values.replaceAll(this::remapAnnotationValue);
        }

        return annotationNode;
    }

    @SuppressWarnings("unchecked")
    private Object remapAnnotationValue(Object value) {
        switch (value) {
            case Type type -> {
                return remapType(type);
            }
            case AnnotationNode inner -> {
                return remapAnnotation(inner);
            }
            case List list -> {
                for (int i1 = 0; i1 < list.size(); i1++) {
                    list.set(i1, remapAnnotationValue(list.get(i1)));
                }
                return value;
            }
            default -> {
                return value;
            }
        }
    }

    private void remapLdcInsnNode(LdcInsnNode ldcInsnNode) {
        switch (ldcInsnNode.cst) {
            case Handle handle -> ldcInsnNode.cst = remapHandle(handle);
            case Type type -> ldcInsnNode.cst = remapType(type);
            case ConstantDynamic constantDynamic -> {
                Handle bsm = remapHandle(constantDynamic.getBootstrapMethod());
                List<Object> bsmArgs = new ArrayList<>();
                for (int i = 0; i < constantDynamic.getBootstrapMethodArgumentCount(); i++) {
                    switch (constantDynamic.getBootstrapMethodArgument(i)) {
                        case Handle handle -> {
                            bsmArgs.add(remapHandle(handle));
                        }
                        case Type type -> {
                            bsmArgs.add(remapType(type));
                        }
                        default -> System.out.println("Ignored BSM-Argument");
                    }
                }

                ldcInsnNode.cst = new ConstantDynamic(
                        constantDynamic.getName(),
                        remapMethodDesc(DescriptorParser.parseMethodDesc(constantDynamic.getDescriptor())).toDesc(),
                        bsm,
                        bsmArgs
                );
            }
            default -> {
            }
        }
    }

    private void remapFieldInsnNode(FieldInsnNode target) {
        target.owner = remapDescMember(DescriptorParser.parseType(target.owner)).toType();
        target.desc = remapDescMember(DescriptorParser.parseFieldDesc(target.desc)).toDesc();
    }

    private void remapMethodInsnNode(MethodInsnNode target) {
        target.owner = remapDescMember(DescriptorParser.parseType(target.owner)).toType();
        target.desc = remapMethodDesc(DescriptorParser.parseMethodDesc(target.desc)).toDesc();
    }

    private Handle remapHandle(Handle target) {
        Type t = Type.getType(target.getDesc());
        String remappedDesc = t.getSort() == Type.METHOD ?
                remapMethodDesc(DescriptorParser.parseMethodDesc(target.getDesc())).toDesc()
                :
                remapDescMember(DescriptorParser.parseMembers(target.getDesc()).getFirst()).toDesc();

        return new Handle(
                target.getTag(),
                getMapped(target.getOwner()),
                target.getName(),
                remappedDesc,
                target.isInterface()
        );
    }

    private Type remapType(Type target) {
        if (target.getSort() == Type.METHOD) {
            return Type.getType(
                    remapMethodDesc(
                            DescriptorParser.parseMethodDesc(target.getDescriptor())
                    ).toDesc()
            );
        }

        return Type.getType(
                remapDescMember(
                        DescriptorParser.parseMember(target.getDescriptor())
                ).toDesc()
        );
    }

    private String remapMethodDescStr(MethodDescriptor target) {
        return remapMethodDesc(target).toDesc();
    }

    private MethodDescriptor remapMethodDesc(MethodDescriptor target) {
        target.getParams().forEach(this::remapDescMember);
        remapDescMember(target.getReturnType());

        return target;
    }

    private DescriptorMember remapDescMember(DescriptorMember target) {
        if (!target.isPrimitive()) {
            target.setValue(getMapped(target.getValue()));
        }

        return target;
    }

    private String getMapped(String original) {
        return mapping.getOrDefault(original, original);
    }
}
