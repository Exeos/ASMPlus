package me.exeos.asmplus.utils;

import me.exeos.asmplus.analysis.hierarchy.edge.ClassEdge;
import me.exeos.asmplus.analysis.hierarchy.edge.MethodEdge;
import me.exeos.asmplus.matcher.method.MethodMatchEntry;
import me.exeos.asmplus.matcher.method.MethodMatcher;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class HierarchyUtil {

    public static String genNoneCollidingFieldName(ClassEdge owner, String fieldDesc, Function<Integer, String> nameGen) {
        return genNoneCollidingFieldName(owner, fieldDesc, nameGen, true);
    }

    public static String genNoneCollidingFieldName(ClassEdge owner, String fieldDesc, Function<Integer, String> nameGen, boolean descAware) {
        String name;
        int tryCount = 0;
        do {
            name = nameGen.apply(tryCount);
            tryCount++;
        } while ((descAware ? owner.findNearestField(name, fieldDesc).isPresent() : owner.findNearestField(name).isPresent()));

        return name;
    }

    public static String genNoneCollidingMethodName(ClassEdge owner, String methodDesc, Function<Integer, String> nameGen) {
        return genNoneCollidingMethodName(owner, methodDesc, Set.of(), nameGen);
    }

    public static String genNoneCollidingMethodName(ClassEdge owner, String methodDesc, Set<String> excludedNames, Function<Integer, String> nameGen) {
        String name;
        int tryCount = 0;
        do {
            name = nameGen.apply(tryCount);
            tryCount++;
        } while (excludedNames.contains(name) || owner.findNearestMethod(name, methodDesc).isPresent());

        return name;
    }

    public static void hierarchyExpandMethodMatcher(MethodMatcher matcher, Map<String, ClassEdge> hierarchy) {
        for (MethodMatchEntry wrapper : matcher.get().toArray(new MethodMatchEntry[0])) {
            if (!hierarchy.containsKey(wrapper.owner())) {
                continue;
            }

            for (MethodEdge declaringEdge : hierarchy.get(wrapper.owner()).findTopDeclarations(wrapper.name(), wrapper.desc())) {
                matcher.add(MethodMatchEntry.of(declaringEdge));
                declaringEdge.getOverriders().forEach(overrider -> matcher.add(MethodMatchEntry.of(overrider)));
            }
        }
    }
}
