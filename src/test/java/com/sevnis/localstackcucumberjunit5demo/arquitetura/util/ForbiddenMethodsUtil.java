package br.tur.reservafacil.precificador.arquitetura.util;

import br.tur.reservafacil.precificador.domain.component.aereo.AeroportoCacheComponent;
import br.tur.reservafacil.precificador.domain.repository.Repository;
import br.tur.reservafacil.precificador.infrastructure.database.jooq.dao.mapper.CustomMapper;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Created by fepelichero on 20/08/2018
 */
public class ForbiddenMethodsUtil {

    public static final Class[] ACCEPTED_ANNOTATION = {Service.class, Component.class};

    public static final Class[] ACCEPTED_PARENT = {Repository.class, CustomMapper.class};

    public static final String[] EXCLUDED_CLASSES = {AeroportoCacheComponent.class.getSimpleName()};

    public static boolean mustBeCached(JavaMethodCall javaMethodCall) {
        return !javaMethodCall.getOriginOwner().isAssignableTo(Repository.class) && javaMethodCall.getTargetOwner().isAssignableTo(Repository.class);
    }

    public static boolean isNotCached(JavaMethodCall javaMethodCall) {
        return !isCached(javaMethodCall);
    }

    public static boolean isCached(JavaMethodCall javaMethodCall) {
        return mustBeCached(javaMethodCall) && javaMethodCall.getOrigin().isAnnotatedWith(Cacheable.class);
    }
}
