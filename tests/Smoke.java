import com.indea.util.SeguridadUtil;
import com.indea.util.Sesion;
import com.indea.modelo.Rol;
public class Smoke {public static void main(String[] a){String h=SeguridadUtil.hashear("demo123");if(!SeguridadUtil.verificar("demo123",h)||SeguridadUtil.verificar("incorrecta",h))throw new AssertionError();try{Sesion.exigirRol(Rol.ADMINISTRADOR);throw new AssertionError();}catch(com.indea.excepcion.AccesoDenegadoException expected){}System.out.println("OK: hash, contraseña incorrecta, acceso sin sesión");}}
