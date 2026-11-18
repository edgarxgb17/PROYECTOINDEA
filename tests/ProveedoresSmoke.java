package com.indea.servicio;

import com.indea.excepcion.ValidacionException;

public final class ProveedoresSmoke {
    public static void main(String[] args) {
        var ficha=ProveedorService.preparar("  Equipos   de Xalapa  "," María ","2288374101",
                "maria.xalapa@gmail.com","Centro, Xalapa, Veracruz");
        comprobar(ficha.nombre().equals("Equipos de Xalapa") && ficha.contacto().equals("María"));
        comprobar(ProveedorService.preparar("Suministros",null,"",null,null).telefono()==null);
        comprobar(new NegocioService.ProveedorCompra(2,null).id()==2);
        comprobar(new NegocioService.ProveedorCompra(null,ficha).nueva()==ficha);
        comprobar(NegocioService.cantidadEquipos(" 5 ")==5 && NegocioService.cantidadEquipos("1")==1);
        rechaza(() -> ProveedorService.preparar("",null,null,null,null),ValidacionException.class);
        rechaza(() -> ProveedorService.preparar("Equipos",null,null,"invalido",null),ValidacionException.class);
        rechaza(() -> new NegocioService.ProveedorCompra(null,null),IllegalArgumentException.class);
        rechaza(() -> new NegocioService.ProveedorCompra(2,ficha),IllegalArgumentException.class);
        for (String valor : new String[]{"0","-1","1.5","","2147483648"}) {
            rechaza(() -> NegocioService.cantidadEquipos(valor),IllegalArgumentException.class);
        }
        System.out.println("OK: ficha de proveedor, alta o selección y cantidades de compra");
    }
    static void comprobar(boolean bien) {
        if (!bien) throw new AssertionError("Resultado incorrecto");
    }
    static void rechaza(Runnable paso,Class<? extends RuntimeException> tipo) {
        try { paso.run(); }
        catch (RuntimeException e) { if (tipo.isInstance(e)) return; throw e; }
        throw new AssertionError("Faltó validación");
    }
}
