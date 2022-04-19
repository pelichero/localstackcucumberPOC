package br.tur.reservafacil.precificador.arquitetura.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static br.tur.reservafacil.precificador.arquitetura.util.LayersEnum.*;

/**
 * Created by fepelichero on 21/08/2018
 */
public enum PackageEnum {

    CONTROLLER_PACKAGE("br.tur.reservafacil.precificador.infrastructure.controller..", "..controller..", CONTROLLER),

    SERVICE_PACKAGE("br.tur.reservafacil.precificador.application.service..", "..service..", SERVICE),

    REPOSITORY_DOMAIN_PACKAGE("br.tur.reservafacil.precificador.domain.repository..", "..repository..", REPOSITORY),

    REPOSITORY_INFRASTRUCTURE_PACKAGE("br.tur.reservafacil.precificador.infrastructure.database..", "..repository..", REPOSITORY),

    REPOSITORY_INFRASTRUCTURE_BIGDATA_PACKAGE("br.tur.reservafacil.precificador.infrastructure.bigdata..", "..repository..", REPOSITORY),

    CONFIGURATION_INFRASTRUCTURE_PACKAGE("br.tur.reservafacil.precificador.infrastructure.config..", "..config..", CONFIGURATION);

    private String path;

    private String genericPath;

    private LayersEnum layer;

    PackageEnum(String path, String genericPath, LayersEnum layer) {
        this.path = path;
        this.genericPath = genericPath;
        this.layer = layer;
    }

    public static List<PackageEnum> byLayer(LayersEnum layer){
        return Arrays.stream(PackageEnum.values()).filter(packageEnum -> packageEnum.getLayer().equals(layer)).collect(Collectors.toList());
    }

    public static String[] pathsPerLayer(LayersEnum layer){
	return Arrays.stream(PackageEnum.values()).filter(packageEnum -> packageEnum.getLayer().equals(layer)).map(PackageEnum::getPath).toArray(size -> new String[size]);
    }

    public static String[] genericPathsPerLayer(LayersEnum layer){
        return Arrays.stream(PackageEnum.values()).filter(packageEnum -> packageEnum.getLayer().equals(layer)).map(PackageEnum::getGenericPath).toArray(size -> new String[size]);
    }

    public String getPath() {
	return path;
    }

    public String getGenericPath() {
        return genericPath;
    }

    public LayersEnum getLayer() {
	return layer;
    }
}
