package com.indea.util;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Exportación tabular en PDF con fuentes estándar, sin dependencias adicionales. */
public final class ReportePdf {
    public record Columna(String titulo, int ancho) {}
    public record Informe(String titulo, String alcance, List<String> notas, List<Columna> columnas,
                          List<List<String>> filas) {
        public Informe {
            notas = List.copyOf(notas);
            columnas = List.copyOf(columnas);
            filas = filas.stream().map(List::copyOf).toList();
            int totalColumnas = columnas.size();
            if (columnas.isEmpty() || columnas.stream().anyMatch(c -> c.ancho() < 40)
                    || columnas.stream().mapToInt(Columna::ancho).sum() != 532
                    || filas.stream().anyMatch(f -> f.size() != totalColumnas)) {
                throw new IllegalArgumentException("El formato del reporte no es válido.");
            }
        }
    }

    private static final Charset WIN = Charset.forName("windows-1252");
    private static final Font TEXTO = new Font(Font.SANS_SERIF, Font.PLAIN, 9);
    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String AZUL = "0.12 0.22 0.39", TINTA = "0.10 0.15 0.22";
    private ReportePdf() {}

    public static void guardar(Path destino, Informe informe) throws IOException {
        Path finalPdf = destino.toAbsolutePath();
        Path temporal = Files.createTempFile(finalPdf.getParent(), ".indea-", ".pdf");
        try {
            Files.write(temporal, generar(informe));
            try {
                Files.move(temporal, finalPdf, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporal, finalPdf, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporal);
        }
    }

    static byte[] generar(Informe informe) throws IOException {
        List<StringBuilder> paginas = new ArrayList<>();
        StringBuilder pagina = new StringBuilder();
        paginas.add(pagina);
        int y = cabecera(pagina, informe, true);
        if (informe.filas().isEmpty()) texto(pagina, 48, y - 17, 10, false, TINTA, "No hay registros para este reporte.");
        int numero = 0;
        for (List<String> fila : informe.filas()) {
            List<List<String>> celdas = new ArrayList<>();
            int lineas = 1;
            for (int c=0; c<fila.size(); c++) {
                List<String> trozos = ajustar(fila.get(c), informe.columnas().get(c).ancho() - 12, 3);
                celdas.add(trozos);
                lineas = Math.max(lineas, trozos.size());
            }
            int alto = 9 + 12 * lineas;
            if (y - alto < 48) {
                pagina = new StringBuilder();
                paginas.add(pagina);
                y = cabecera(pagina,informe,false);
            }
            if (numero++ % 2 == 0) caja(pagina,40,y-alto,532,alto,"0.95 0.97 0.99");
            int x = 40;
            for (int c=0; c<celdas.size(); c++) {
                int ancho = informe.columnas().get(c).ancho();
                pagina.append("q ").append(x+3).append(' ').append(y-alto+1).append(' ')
                        .append(ancho-6).append(' ').append(alto-2).append(" re W n\n");
                for (int i=0; i<celdas.get(c).size(); i++) texto(pagina,x+6,y-14-i*12,9,false,TINTA,celdas.get(c).get(i));
                pagina.append("Q\n");
                x += ancho;
            }
            y -= alto;
        }

        String estampado = "Generado " + LocalDateTime.now().format(FECHA);
        List<byte[]> objetos = new ArrayList<>();
        objetos.add(bytes("<< /Type /Catalog /Pages 2 0 R >>"));
        StringBuilder hijos = new StringBuilder();
        for (int p=0; p<paginas.size(); p++) hijos.append(5+p*2).append(" 0 R ");
        objetos.add(bytes("<< /Type /Pages /Count "+paginas.size()+" /Kids ["+hijos+"] >>"));
        objetos.add(bytes("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>"));
        objetos.add(bytes("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>"));
        for (int p=0; p<paginas.size(); p++) {
            var contenido = paginas.get(p);
            caja(contenido,40,40,532,1,"0.81 0.85 0.90");
            texto(contenido,40,27,8,false,AZUL,estampado);
            texto(contenido,477,27,8,false,AZUL,"Página "+(p+1)+" de "+paginas.size());
            int num = 6+p*2;
            objetos.add(bytes("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] "
                    +"/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents "+num+" 0 R >>"));
            byte[] datos = bytes(contenido.toString());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.write(bytes("<< /Length "+datos.length+" >>\nstream\n"));
            stream.write(datos);
            stream.write(bytes("\nendstream"));
            objetos.add(stream.toByteArray());
        }
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        salida.write(bytes("%PDF-1.4\n%âãÏÓ\n"));
        List<Integer> posiciones = new ArrayList<>();
        for (int i=0; i<objetos.size(); i++) {
            posiciones.add(salida.size());
            salida.write(bytes((i+1)+" 0 obj\n"));
            salida.write(objetos.get(i));
            salida.write(bytes("\nendobj\n"));
        }
        int referencia = salida.size();
        salida.write(bytes("xref\n0 "+(objetos.size()+1)+"\n0000000000 65535 f \n"));
        for (int pos : posiciones) salida.write(bytes(String.format(Locale.ROOT,"%010d 00000 n \n",pos)));
        salida.write(bytes("trailer\n<< /Size "+(objetos.size()+1)+" /Root 1 0 R >>\nstartxref\n"+referencia+"\n%%EOF\n"));
        return salida.toByteArray();
    }

    private static int cabecera(StringBuilder p, Informe informe, boolean primera) {
        caja(p,40,692,532,68,AZUL);
        texto(p,54,740,10,true,"0.86 0.92 1","INDEA   /   REPORTE OPERATIVO");
        texto(p,54,714,19,true,"1 1 1",informe.titulo());
        int y = 673;
        for (String l : ajustar(informe.alcance(),510,2)) { texto(p,40,y,9,true,TINTA,l); y-=13; }
        if (primera) for (String nota : informe.notas()) {
            for (String l : ajustar(nota,515,3)) { texto(p,40,y,9,false,TINTA,l); y-=12; }
        }
        y -= 10;
        caja(p,40,y-24,532,24,AZUL);
        int x=40;
        for (Columna c : informe.columnas()) {
            texto(p,x+6,y-15,9,true,"1 1 1",ajustar(c.titulo(),c.ancho()-12,1).get(0));
            x+=c.ancho();
        }
        return y-24;
    }

    private static List<String> ajustar(String s, int ancho, int max) {
        String resto = s == null ? "" : s.replaceAll("[\\p{Cntrl}\\s]+"," ").strip();
        List<String> lineas = new ArrayList<>();
        while (!resto.isEmpty() && lineas.size()<max) {
            int n=resto.length();
            while (n>1 && TEXTO.getStringBounds(resto.substring(0,n),FRC).getWidth()>ancho) n--;
            if (n<resto.length() && lineas.size()==max-1) {
                while (n>1 && TEXTO.getStringBounds(resto.substring(0,n)+"…",FRC).getWidth()>ancho) n--;
                lineas.add(resto.substring(0,n).stripTrailing()+"…");
                break;
            }
            if (n<resto.length() && resto.charAt(n)!=' ') {
                int espacio=resto.lastIndexOf(' ',n-1);
                if (espacio>0) n=espacio;
            }
            lineas.add(resto.substring(0,n).stripTrailing());
            resto=resto.substring(n).stripLeading();
        }
        return lineas.isEmpty()?List.of(""):lineas;
    }

    private static byte[] bytes(String s) { return s.getBytes(WIN); }
    private static void caja(StringBuilder p,int x,int y,int w,int h,String color) {
        p.append(color).append(" rg ").append(x).append(' ').append(y).append(' ').append(w).append(' ')
                .append(h).append(" re f\n");
    }
    private static void texto(StringBuilder p,int x,int y,int tam,boolean negrita,String color,String s) {
        String limpio=s.replaceAll("[\\p{Cntrl}]"," ").replace("\\","\\\\").replace("(","\\(").replace(")","\\)");
        p.append(color).append(" rg BT /").append(negrita?"F2":"F1").append(' ').append(tam).append(" Tf ")
                .append(x).append(' ').append(y).append(" Td (").append(limpio).append(") Tj ET\n");
    }
}
