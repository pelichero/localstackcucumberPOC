package br.tur.reservafacil.precificador.arquitetura.condition;

import br.tur.reservafacil.precificador.arquitetura.util.CriticalMethodsLogUtil;
import br.tur.reservafacil.precificador.arquitetura.util.ForbiddenMethodsLogUtil;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static br.tur.reservafacil.precificador.arquitetura.util.ForbiddenMethodsLogUtil.errorMessage;
import static br.tur.reservafacil.precificador.arquitetura.util.ForbiddenMethodsLogUtil.printHead;
import static br.tur.reservafacil.precificador.arquitetura.util.ForbiddenMethodsUtil.*;
import static br.tur.reservafacil.precificador.arquitetura.util.PackagesUtil.MAIN_PACKAGE;
import static br.tur.reservafacil.precificador.arquitetura.util.PackagesUtil.ROOT_PACKAGE;

/**
 * Created by fepelichero on 21/08/2018
 */
public class ForbiddenMethodsCondition
                extends ArchCondition<JavaClass> {

    public static final Predicate<JavaMethodCall> CLASS_IS_QUALIFIED_PER_INHERITANCE_PREDICATE = call -> Stream.of(ACCEPTED_ANNOTATION).anyMatch(stereotype -> call.getOriginOwner().isAnnotatedWith(stereotype)) || Arrays.stream(ACCEPTED_PARENT).anyMatch(parent -> call.getOriginOwner().isAssignableTo(parent));

    public static final Predicate<JavaMethodCall> EXCLUDED_CLASSES_PREDICATE     = call -> Stream.of(EXCLUDED_CLASSES).noneMatch(clazz -> call.getTargetOwner().getSimpleName().equals(clazz));

    public static final String FORBIDDEN_VALUE_OF = "valueOf";

    private static List<JavaMethodCall> alreadyPassed = new ArrayList<>();

    private static JavaClasses javaClasses = new ClassFileImporter().importPackages(MAIN_PACKAGE);

    public ForbiddenMethodsCondition() {
        super("not perform forbidden methods. ");
    }

    @Override public void check(JavaClass javaClass, ConditionEvents conditionEvents) {
        List<JavaMethodCall> methodCalls = validateForbiddenMethodCalls(javaClass, conditionEvents);

        List<JavaMethodCall> forbiddenMethods = forbiddenMethods(methodCalls);

        forbiddenMethods.forEach(m -> conditionEvents.add(SimpleConditionEvent.violated(javaClass, errorMessage(javaClass, m))));
    }

    private static List<JavaMethodCall> forbiddenMethods(List<JavaMethodCall> methodCalls) {
        return Optional.of(methodCalls)
                        .map(calls -> calls
                                        .stream()
                                        .filter(ForbiddenMethodsCondition::forbidden)
                                        .collect(Collectors.toList()))
                        .orElse(Collections.emptyList());
    }

    private static boolean forbidden(JavaMethodCall javaMethodCall) {
        return javaMethodCall.getTarget().getOwner().getPackage().contains(ROOT_PACKAGE) && javaMethodCall.getName().contains(FORBIDDEN_VALUE_OF);
    }

    private static List<JavaMethodCall> validateForbiddenMethodCalls(JavaClass javaClass, ConditionEvents conditionEvents) {
        return validateForbiddenMethodCalls(javaClass, conditionEvents, null);
    }

    private static List<JavaMethodCall> validateForbiddenMethodCalls(JavaClass javaClass, ConditionEvents conditionEvents, JavaMethodCall javaMethodCall) {

        printHead(javaClass);

        return javaClass
                        .getMethods()
                        .stream()
                        .map(ForbiddenMethodsCondition::getMethodCalls)
                        .flatMap(Collection::stream)
                        .peek(ForbiddenMethodsLogUtil::debugMethodCall)
                        .flatMap(call -> methodCallTree(call, conditionEvents).stream())
                        .collect(Collectors.toList());
    }

    private static List<JavaMethodCall> methodCallTree(JavaMethodCall methodCall, ConditionEvents conditionEvents){

        List<JavaMethodCall> javaMethodCallStream = methodCall
                        .getTargetOwner()
                        .getMethods()
                        .stream()
                        .filter(matchSpecificMethod(methodCall))
                        .map(ForbiddenMethodsCondition::getMethodCalls)
                        .map(methodCalls -> removeRecursiveMethodCalls(methodCall, methodCalls))
                        .flatMap(Collection::stream)
                        .filter(EXCLUDED_CLASSES_PREDICATE)
                        .peek(CriticalMethodsLogUtil::debugMethodCall)
                        .filter(CLASS_IS_QUALIFIED_PER_INHERITANCE_PREDICATE)
                        .flatMap(call ->  methodCallTree(call, conditionEvents).stream())
                        .collect(Collectors.toList());
        javaMethodCallStream.add(methodCall);

        if(isInterfaceAndNotScannedYet(methodCall)){

            alreadyPassed.add(methodCall);

            Stream<JavaClass>
                            classes = StreamSupport.stream(Spliterators.spliteratorUnknownSize(javaClasses.iterator(), Spliterator.ORDERED), false)
                            .filter(javaClass -> !javaClass.isInterface())
                            .filter(javaClass -> !methodCall.getTargetOwner().getSimpleName().equals(javaClass.getSimpleName()))
                            .filter(javaClass -> javaClass.isAssignableTo(methodCall.getTargetOwner().reflect()))
                            .distinct();

            javaMethodCallStream.addAll(classes.flatMap(javaClass -> validateForbiddenMethodCalls(javaClass, conditionEvents, methodCall).stream()).collect(Collectors.toList()));
        }

        return javaMethodCallStream;
    }

    private static boolean isInterfaceAndNotScannedYet(JavaMethodCall methodCall) {
        return methodCall.getTargetOwner().isInterface()
                        && alreadyPassed.stream()
                        .noneMatch(m -> methodCall.getOriginOwner().getSimpleName().equals(m.getOriginOwner().getSimpleName())
                                        && methodCall.getTargetOwner().getSimpleName().equals(m.getTargetOwner().getSimpleName())
                                        && methodCall.getName().equals(m.getName()));
    }

    private static Set<JavaMethodCall> removeRecursiveMethodCalls(JavaMethodCall methodCall, Set<JavaMethodCall> methodCalls) {
        Set<JavaMethodCall> noRecursiveMethods = new HashSet<>(methodCalls);
        noRecursiveMethods.removeIf(removeRecursiveMethodCallsPredicate(methodCall));
        return noRecursiveMethods;
    }

    private static Predicate<JavaMethodCall> removeRecursiveMethodCallsPredicate(JavaMethodCall methodCall) {
        return m -> methodCall.getOriginOwner().getName().equals(m.getOriginOwner().getName()) && methodCall.getName().equals(m.getName());
    }

    private static Predicate<JavaMethod> matchSpecificMethod(JavaMethodCall javaMethodCall) {
        return javaMethod -> javaMethodCall.getTarget().getName().equals(javaMethod.getName());
    }

    private static Set<JavaMethodCall> getMethodCalls(JavaMethod javaMethod){
        return Collections.unmodifiableSet(javaMethod.getMethodCallsFromSelf());
    }
}
