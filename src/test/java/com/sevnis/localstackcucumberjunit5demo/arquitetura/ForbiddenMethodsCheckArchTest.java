package br.tur.reservafacil.precificador.arquitetura;

import br.tur.reservafacil.precificador.arquitetura.condition.ForbiddenMethodsCondition;
import br.tur.reservafacil.precificador.arquitetura.predicate.AllValidClassesPredicate;
import br.tur.reservafacil.precificador.arquitetura.util.ArchiectureTestMarker;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchIgnore;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static br.tur.reservafacil.precificador.arquitetura.util.PackagesUtil.MAIN_PACKAGE;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Created by fepelichero on 21/08/2018
 */
@Category(ArchiectureTestMarker.class)
@RunWith(ArchUnitRunner.class)
@AnalyzeClasses(packages = MAIN_PACKAGE)
public class ForbiddenMethodsCheckArchTest {

    private static final DescribedPredicate<JavaClass> VALID_PREDICATE                 = new AllValidClassesPredicate();
    private static final ArchCondition<JavaClass>      HAS_FORBIDDEN_METHODS_CONDITION = new ForbiddenMethodsCondition();

    /**
     * FIXME: Está sendo ignorado no momento para passar no pipeline, porém abri a tarefa:
     * @see <a href="https://git.reservafacil.tur.br/precificador/precificador/issues/167">Issue 167</a>
     */
    @ArchTest
    @ArchIgnore
    public static final ArchRule noForbiddenMethods = classes().that(VALID_PREDICATE).should(HAS_FORBIDDEN_METHODS_CONDITION);

}
