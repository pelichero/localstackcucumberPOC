package br.tur.reservafacil.precificador.arquitetura.util;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.List;

/**
 * Created by fepelichero on 20/08/2018
 */
public class ForbiddenMethodsLogUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ForbiddenMethodsLogUtil.class);

    public static String errorMessage(JavaClass javaClass, JavaMethodCall methodCall) {
        return MessageFormat.format("\n Classe {0} contém métodos não permitidos {1} ", javaClass.getName(), formatMethodCall(methodCall));
    }

    public static void printHead(JavaClass clazz){
        LOG.info("");
        LOG.info(MessageFormat.format("Avaliando métodos não permitidos na classe: {0}", clazz.getSimpleName()));
    }

    public static void printFooter(JavaClass clazz, List<JavaMethodCall> checkedMethods, List<JavaMethodCall> methodsThatMustBeCached) {
        LOG.info("");
        LOG.info(MessageFormat.format("{0} -> Métodos checados -> {1} -> Metodos não permitidos {2}", clazz.getSimpleName(), checkedMethods.size(), methodsThatMustBeCached.size()));
    }

    public static void debugMethodCall(JavaMethodCall javaMethod){
//        LOG.debug(MessageFormat.format("{0}     {1}", String.format("%1$6s", "####"), formatMethodCall(javaMethod)));
    }

    private static String formatMethodCall(JavaMethodCall javaMethod) {
        String origin = String.format("%1$52s", javaMethod.getOriginOwner().getSimpleName());
        String originMethod = String.format("%1$46s", javaMethod.getOrigin().getName());
        String targetMethod = javaMethod.getTarget().getName();
        String targetOwner = javaMethod.getTarget().getOwner().getSimpleName();
        boolean targetOwnerIsEnum = javaMethod.getTarget().getOwner().isAssignableTo(Enum.class);
        String packageS = javaMethod.getTarget().getOwner().getPackage();
        return MessageFormat.format("\n {0} -> {1} -> {3}.{2} [{4}] ", origin, originMethod, targetMethod, targetOwner, packageS);
    }

}
