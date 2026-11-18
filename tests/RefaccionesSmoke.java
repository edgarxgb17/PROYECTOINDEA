package com.indea.servicio;

import com.indea.excepcion.AccesoDenegadoException;
import com.indea.excepcion.ValidacionException;
import com.indea.modelo.Empleado;
import com.indea.modelo.Rol;
import com.indea.modelo.Usuario;
import com.indea.util.Sesion;

import java.math.BigDecimal;

public final class RefaccionesSmoke {
    public static void main(String[] args) {
        check(RefaccionService.validarCantidad("0", true) == 0);
        check(RefaccionService.validarCantidad(" 2 ", false) == 2);
        check(RefaccionService.validarCosto("350.5").equals(new BigDecimal("350.50")));
        check(RefaccionService.validarCosto("0").equals(new BigDecimal("0.00")));
        espera(ValidacionException.class, () -> RefaccionService.validarCantidad("0", false));
        espera(ValidacionException.class, () -> RefaccionService.validarCantidad("-2", true));
        espera(ValidacionException.class, () -> RefaccionService.validarCantidad("1.5", false));
        espera(ValidacionException.class, () -> RefaccionService.validarCantidad("2147483648", false));
        espera(ValidacionException.class, () -> RefaccionService.validarCosto("12.345"));
        espera(ValidacionException.class, () -> RefaccionService.validarCosto("100000000"));
        RefaccionService s = new RefaccionService();
        Sesion.cerrar();
        espera(AccesoDenegadoException.class, s::listar);
        iniciar(Rol.TECNICO);
        espera(AccesoDenegadoException.class, () -> s.registrar("Pantalla", "650", "3"));
        espera(AccesoDenegadoException.class, () -> s.reponer(1, "1"));
        espera(ValidacionException.class, () -> s.asignar("F01", 1, "0"));
        iniciar(Rol.VENDEDOR);
        espera(AccesoDenegadoException.class, () -> s.asignar("F01", 1, "1"));
        Sesion.cerrar();
        System.out.println("OK: costos, cantidades, acceso y validación de refacciones");
    }

    private static void iniciar(Rol rol) {
        Empleado empleado = new Empleado();
        empleado.setRol(rol);
        Usuario usuario = new Usuario();
        usuario.setEmpleado(empleado);
        Sesion.iniciar(usuario);
    }

    private static void check(boolean ok) {
        if (!ok) throw new AssertionError("Falló la comprobación");
    }

    private static void espera(Class<? extends RuntimeException> clase, Runnable accion) {
        try {
            accion.run();
        } catch (RuntimeException e) {
            if (clase.isInstance(e)) return;
            throw e;
        }
        throw new AssertionError("Se esperaba " + clase.getSimpleName());
    }
}
