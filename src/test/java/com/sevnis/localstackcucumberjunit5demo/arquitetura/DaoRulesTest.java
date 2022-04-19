package br.tur.reservafacil.precificador.arquitetura;

import br.tur.reservafacil.precificador.arquitetura.util.ArchiectureTestMarker;
import br.tur.reservafacil.precificador.domain.repository.Repository;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static br.tur.reservafacil.precificador.arquitetura.util.LayersEnum.REPOSITORY;
import static br.tur.reservafacil.precificador.arquitetura.util.PackageEnum.pathsPerLayer;
import static br.tur.reservafacil.precificador.arquitetura.util.PackagesUtil.MAIN_PACKAGE;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Created by fepelichero on 21/08/2018
 */
@Category(ArchiectureTestMarker.class)
@RunWith(ArchUnitRunner.class)
@AnalyzeClasses(packages = MAIN_PACKAGE, importOptions = ImportOption.DontIncludeTests.class)
public class DaoRulesTest {

    @ArchTest
    public static final ArchRule testaRepositoriesNoPacoteCorreto =
		    classes()
			.that()
			.areAssignableTo(Repository.class)
			.should()
			.resideInAnyPackage(pathsPerLayer(REPOSITORY))
			.as("Entidades devem estar no pacote 'domain'");
}
