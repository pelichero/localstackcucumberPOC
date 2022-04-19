package br.tur.reservafacil.precificador.arquitetura.predicate;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.ArchIgnore;

import static br.tur.reservafacil.precificador.arquitetura.util.PackagesUtil.MAIN_PACKAGE;

/**
 * Created by fepelichero on 21/08/2018
 */
public class AllValidClassesPredicate
		extends DescribedPredicate<JavaClass> {

  public AllValidClassesPredicate() {
    super("resides in package " + MAIN_PACKAGE);
  }

  @Override
  public boolean apply(JavaClass javaClass) {
    return !javaClass.isAnnotatedWith(ArchIgnore.class);
  }
}
