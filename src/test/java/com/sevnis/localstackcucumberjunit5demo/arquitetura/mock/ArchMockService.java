package br.tur.reservafacil.precificador.arquitetura.mock;

import br.tur.reservafacil.dominio.arquitetura.CriticalPerformance;
import com.newrelic.api.agent.Trace;
import com.tngtech.archunit.junit.ArchIgnore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * @implNote Serviço apenas para testes da varredura de metodos com o ArchUnit
 * Created by fepelichero on 22/08/2018.
 */
@Service
@ArchIgnore
public class ArchMockService {

    @Autowired
    private ArchMockServiceInjected serviceInjected;

    @Autowired
    private ArchMockInterfaceInjected interfaceInjected;

    /**
     * FIXME:
     * O ideal é colocar a anotação @CriticalPerformance nesse método de entrada
     * porém não consegui encontrar uma maneira prática de obter
     * métodos chamados dentro de uma lambda via reflexão.
     *
     * Uma forma paliativa de resolver isso é anotar com @CriticalPerformance também o método invocado dentro da lambda
     */
    @CriticalPerformance
    public void criticalMethod() {
        new ArrayList<>().forEach(v -> this.innerAccessMethod());
    }

    @Trace
    @CriticalPerformance
    private void innerAccessMethod() {
        this.methodThatAccesAnotherService();
    }

    private void methodThatAccesAnotherService() {
        interfaceInjected.mustBeCachedButIsNotMethod();
        serviceInjected.mustBeCachedButIsNotMethod();
        serviceInjected.mustBeCachedAndIs();
        serviceInjected.mustBeNotCachedMethod();
    }
}