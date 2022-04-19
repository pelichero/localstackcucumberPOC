package br.tur.reservafacil.precificador.arquitetura.mock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by fepelichero on 24/08/2018
 */
@Service
public class ArchMockImplInjected implements ArchMockInterfaceInjected {

    @Autowired
    private ArchMockRepository repository;

    public void mustBeCachedButIsNotMethod(){
	repository.findAll();
    }
}
