package com.indea.vista;

import com.indea.servicio.NegocioService.PersonaOperacion;

public final class SelectoresSmoke {
    public static void main(String[] args) {
        check(SelectorOperacion.coincide("María Hernández · 2281234567 · mh@example.test",
                "2281234567", "maria hernandez"));
        check(SelectorOperacion.coincide("María · 2281234567", "2281234567", "(228) 123-45"));
        check(SelectorOperacion.coincide("María · mh@example.test", null, "MH@EXAMPLE"));
        check(SelectorOperacion.coincide("Laptop Lenovo ThinkPad T480", null, "t480"));
        check(!SelectorOperacion.coincide("Laptop Lenovo ThinkPad T480", null, "HP"));
        check(PersonaOperacion.nueva("María", "2281234567", "") != null);
        check(PersonaOperacion.nueva("María", "", "mh@example.test") != null);
        check(PersonaOperacion.existente(2) != null);
        try {
            PersonaOperacion.nueva("María", "", "");
            throw new AssertionError("Aceptó un alta sin contacto");
        } catch (com.indea.excepcion.ValidacionException expected) {
            // Debe exigir al menos un medio de contacto.
        }
        System.out.println("OK: selector por nombre, teléfono, correo y equipo; reglas de alta rápida");
    }

    private static void check(boolean ok) {
        if (!ok) throw new AssertionError("Falló un caso del selector");
    }
}
