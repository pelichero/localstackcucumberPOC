package br.tur.reservafacil.precificador.arquitetura;

import br.tur.reservafacil.precificador.arquitetura.util.ArchiectureTestMarker;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchIgnore;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static br.tur.reservafacil.precificador.arquitetura.util.LayersEnum.*;
import static br.tur.reservafacil.precificador.arquitetura.util.PackageEnum.pathsPerLayer;
import static br.tur.reservafacil.precificador.arquitetura.util.PackagesUtil.MAIN_PACKAGE;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Created by fepelichero on 21/08/2018
 */
@Category(ArchiectureTestMarker.class)
@RunWith(ArchUnitRunner.class)
@AnalyzeClasses(packages = MAIN_PACKAGE, importOptions = ImportOption.DontIncludeTests.class)
@ArchIgnore
public class LayerRulesTest {

    @ArchTest
    public static final ArchRule testaAcessoEntreCamadas = layeredArchitecture()
        .layer("Controllers").definedBy(pathsPerLayer(CONTROLLER))
        .layer("Services").definedBy(pathsPerLayer(SERVICE))
        .layer("Repository").definedBy(pathsPerLayer(REPOSITORY))

        .whereLayer("Controllers").mayNotBeAccessedByAnyLayer()
        .whereLayer("Services").mayOnlyBeAccessedByLayers("Controllers")
        .whereLayer("Repository").mayOnlyBeAccessedByLayers("Services");
}