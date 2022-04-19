package br.tur.reservafacil.precificador.arquitetura;

import br.tur.reservafacil.precificador.arquitetura.condition.CalledMethodsIsCachedCondition;
import br.tur.reservafacil.precificador.arquitetura.predicate.CriticalPerformancePredicate;
import br.tur.reservafacil.precificador.arquitetura.util.ArchiectureTestMarker;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static br.tur.reservafacil.precificador.arquitetura.util.PackagesUtil.MAIN_PACKAGE;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 *
 * Teste criado para evitar que um metodo anotado com @CriticalPerformance tem alguma
 * chamada a serviços NÃO cacheada.
 *
 * @see: PrecificadorService#doPrecificacao()
 *
 * Created by fepelichero on 21/08/2018
 */
@Category(ArchiectureTestMarker.class)
@RunWith(ArchUnitRunner.class)
@AnalyzeClasses(packages = MAIN_PACKAGE)
public class CriticalPerformanceServiceArchTest {

    private static final DescribedPredicate<JavaClass> IS_CRITICAL_PERFORMANCE            = new CriticalPerformancePredicate();
    private static final ArchCondition<JavaClass>      CALLED_METHODS_IS_CACHED_CONDITION = new CalledMethodsIsCachedCondition();

    @ArchTest
    public static final ArchRule noCriticalPerformanceMethodCalledWithoutCache = classes().that(IS_CRITICAL_PERFORMANCE).should(CALLED_METHODS_IS_CACHED_CONDITION);

}
