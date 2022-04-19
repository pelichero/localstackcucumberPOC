package br.tur.reservafacil.precificador.arquitetura.mock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ArchMockServiceInjected {

    @Autowired
    private ArchMockRepository repository;

    public void mustBeCachedButIsNotMethod(){
        repository.findAll();
    }

    @Cacheable
    public void mustBeCachedAndIs(){
        repository.findAll();
    }

    public void mustBeNotCachedMethod(){

    }
}
