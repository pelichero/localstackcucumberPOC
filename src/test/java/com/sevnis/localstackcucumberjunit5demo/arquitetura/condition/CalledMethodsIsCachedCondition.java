package br.tur.reservafacil.precificador.arquitetura.condition;

import br.tur.reservafacil.dominio.arquitetura.CriticalPerformance;
import br.tur.reservafacil.precificador.arquitetura.util.CriticalMethodsLogUtil;
import br.tur.reservafacil.precificador.arquitetura.util.CriticalMethodsUtil;
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

import static br.tur.reservafacil.precificador.arquitetura.util.CriticalMethodsLogUtil.*;
import static br.tur.reservafacil.precificador.arquitetura.util.CriticalMethodsUtil.*;
import static br.tur.reservafacil.precificador.arquitetura.util.PackagesUtil.MAIN_PACKAGE;

/**
 * Created by fepelichero on 21/08/2018
 */
public class CalledMethodsIsCachedCondition extends ArchCondition<JavaClass> {

    public static final Predicate<JavaMethodCall> CLASS_IS_QUALIFIED_PER_INHERITANCE_PREDICATE = call -> Stream.of(ACCEPTED_ANNOTATION).anyMatch(stereotype -> call.getOriginOwner().isAnnotatedWith(stereotype)) || Arrays.stream(ACCEPTED_PARENT).anyMatch(parent -> call.getOriginOwner().isAssignableTo(parent));

    public static final Predicate<JavaMethodCall> EXCLUDED_CLASSES_PREDICATE     = call -> Stream.of(EXCLUDED_CLASSES).noneMatch(clazz -> call.getTargetOwner().getSimpleName().equals(clazz));

    private static JavaClasses javaClasses = new ClassFileImporter().importPackages(MAIN_PACKAGE);

    private static List<JavaMethodCall> alreadyPassed = new ArrayList<>();

    public CalledMethodsIsCachedCondition() {
        super("not contains critical methods without cache. ");
    }

    /**
     * Metodos anotados com @CriticalPerformance serão analisados recursivamente
     * para evitar falta de cache
     * @param javaClass
     * @param conditionEvents
     */
    @Override public void check(JavaClass javaClass, ConditionEvents conditionEvents) {
        List<JavaMethodCall> methodCalls = validateCriticalMethodCalls(javaClass, conditionEvents);

        List<JavaMethodCall> methodsThatMustBeCached = mustBeCachedMethods(methodCalls);

        methodsThatMustBeCached.forEach(m -> conditionEvents.add(SimpleConditionEvent.violated(javaClass, errorMessage(javaClass, m))));
    }

    private static List<JavaMethodCall>  validateCriticalMethodCalls(JavaClass javaClass, ConditionEvents conditionEvents) {
        return validateCriticalMethodCalls(javaClass, conditionEvents, null);
    }

    private static List<JavaMethodCall> validateCriticalMethodCalls(JavaClass javaClass, ConditionEvents conditionEvents, JavaMethodCall javaMethodCall) {

        printHead(javaClass);

        return javaClass
                    .getMethods()
                    .stream()
                    .filter(method -> (javaMethodCall == null) ? method.isAnnotatedWith(CriticalPerformance.class) : javaMethodCall.getName().equals(method.getName()))
                    .map(CalledMethodsIsCachedCondition::getMethodCalls)
                    .flatMap(Collection::stream)
                    .filter(EXCLUDED_CLASSES_PREDICATE)
                    .peek(CriticalMethodsLogUtil::debugMethodCall)
                    .filter(CLASS_IS_QUALIFIED_PER_INHERITANCE_PREDICATE)
                    .sorted(orderMethodCall())
                    .flatMap(call -> methodCallTree(call, conditionEvents).stream())
                    .collect(Collectors.toList());
    }

    private static List<JavaMethodCall> methodCallTree(JavaMethodCall methodCall, ConditionEvents conditionEvents){

        prettyPrintDebug(methodCall);

        List<JavaMethodCall> javaMethodCallStream = methodCall
		                                        .getTargetOwner()
		                                        .getMethods()
		                                        .stream()
		                                        .filter(matchSpecificMethod(methodCall))
		                                        .map(CalledMethodsIsCachedCondition::getMethodCalls)
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

	    Stream<JavaClass> classes = StreamSupport.stream(Spliterators.spliteratorUnknownSize(javaClasses.iterator(), Spliterator.ORDERED), false)
			    .filter(javaClass -> !javaClass.isInterface())
			    .filter(javaClass -> !methodCall.getTargetOwner().getSimpleName().equals(javaClass.getSimpleName()))
			    .filter(javaClass -> javaClass.isAssignableTo(methodCall.getTargetOwner().reflect()))
			    .distinct();

	    javaMethodCallStream.addAll(classes.flatMap(javaClass -> validateCriticalMethodCalls(javaClass, conditionEvents, methodCall).stream()).collect(Collectors.toList()));
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

    private static List<JavaMethodCall> mustBeCachedMethods(List<JavaMethodCall> methodCalls) {
	return Optional.of(methodCalls)
			.map(calls -> calls
					.stream()
					.filter(CriticalMethodsUtil::mustBeCached)
					.filter(CriticalMethodsUtil::isNotCached)
					.collect(Collectors.toList()))
			.orElse(Collections.emptyList());
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

    private static Comparator<JavaMethodCall> orderMethodCall() {
	return Comparator.comparing(javaMethodCall -> javaMethodCall.getTargetOwner().getSimpleName());
    }

    private static Set<JavaMethodCall> getMethodCalls(JavaMethod javaMethod){
        return Collections.unmodifiableSet(javaMethod.getMethodCallsFromSelf());
    }
}