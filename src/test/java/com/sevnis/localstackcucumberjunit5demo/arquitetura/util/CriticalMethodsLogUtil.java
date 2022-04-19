package br.tur.reservafacil.precificador.arquitetura.util;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;

import java.text.MessageFormat;
import java.util.List;

import static br.tur.reservafacil.precificador.arquitetura.util.CriticalMethodsUtil.mustBeCached;

/**
 * Created by fepelichero on 20/08/2018
 */
public class CriticalMethodsLogUtil {

    private static final Logger LOG = LoggerFactory.getLogger(CriticalMethodsLogUtil.class);

    public static String errorMessage(JavaClass javaClass, JavaMethodCall methodCall) {
        return MessageFormat.format("\n Classe {0} contém médodos não cacheados {1} ", javaClass.getName(), formatMethodCall(methodCall));
    }

    public static void printHead(JavaClass clazz){
        LOG.info("");
        LOG.info(MessageFormat.format("Avaliando falta de cache na classe: {0}", clazz.getSimpleName()));
    }

    public static void printFooter(JavaClass clazz, List<JavaMethodCall> checkedMethods, List<JavaMethodCall> methodsThatMustBeCached) {
        LOG.info("");
        LOG.info(MessageFormat.format("{0} -> Métodos checados -> {1} -> Metodos não cacheados {2}", clazz.getSimpleName(), checkedMethods.size(), methodsThatMustBeCached.size()));
    }

    public static void debugMethodCall(JavaMethodCall javaMethod){
        LOG.debug(MessageFormat.format("{0}     {1}", String.format("%1$6s", "####"), formatMethodCall(javaMethod)));
    }

    public static String formatMethodCallWithCache(JavaMethodCall javaMethod){
        String cachedStatus = (mustBeCached(javaMethod))
                                ? String.format("%1$6s", javaMethod.getOrigin().isAnnotatedWith(Cacheable.class)
                                    ? "[OK]"
                                    : "[FAIL]")
                                : String.format("%1$6s", "");

        String mustBeCached = String.format("%1$18s", (mustBeCached(javaMethod)) ? "DEVE SER CACHEADO!" : "");
        String origin = String.format("%1$52s", javaMethod.getOriginOwner().getSimpleName());
        String originMethod = String.format("%1$46s", javaMethod.getOrigin().getName());
        String targetMethod = javaMethod.getTarget().getName();
        return MessageFormat.format("{0} {1} {2} -> {3} -> {4} ", mustBeCached, cachedStatus, origin, originMethod, targetMethod);
    }

    private static String formatMethodCall(JavaMethodCall javaMethod) {
        String origin = String.format("%1$52s", javaMethod.getOriginOwner().getSimpleName());
        String originMethod = String.format("%1$46s", javaMethod.getOrigin().getName());
        String targetMethod = javaMethod.getTarget().getName();
        return MessageFormat.format("\n {0} -> {1} -> {2} ", origin, originMethod, targetMethod);
    }

    public static void prettyPrintDebug(JavaMethodCall javaMethod){
        if(mustBeCached(javaMethod))
            LOG.info(formatMethodCallWithCache(javaMethod));
    }
}
