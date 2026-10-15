package mx.unison;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Calculadora {

    private static final String USO = "Uso: Calculadora <documento.xml> <variable1> [variable2]";

    public static void main(String[] args) {
        try {
            validarArgumentos(args);
            ejecutar(args[0], Arrays.asList(args).subList(1, args.length));
        } catch (ErrorCalculadora e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(e.getCodigoSalida());
        }
    }

    private static void validarArgumentos(String[] args) throws ErrorCalculadora {
        if (args.length == 0) {
            throw new ErrorCalculadora(ErrorCalculadora.SIN_ARGUMENTOS,
                    "No se proporcionaron los argumentos requeridos.\n" + USO);
        }
        if (args.length == 1) {
            throw new ErrorCalculadora(ErrorCalculadora.FALTA_VARIABLE,
                    "Debes incluir al menos el nombre de una variable.\n" + USO);
        }
        if (args.length > 3) {
            throw new ErrorCalculadora(ErrorCalculadora.USO_INVALIDO,
                    "Solo se pueden indicar una o dos variables.\n" + USO);
        }
        if (args.length == 3 && args[1].equals(args[2])) {
            throw new ErrorCalculadora(ErrorCalculadora.USO_INVALIDO,
                    "Las dos variables deben ser distintas.\n" + USO);
        }
    }

    private static void ejecutar(String ruta, List<String> variables) throws ErrorCalculadora {
        VariablesHandler handler = new VariablesHandler(variables.toArray(new String[0]));
        leerDocumento(ruta, handler);

        Map<String, List<Double>> datos = new LinkedHashMap<>();
        for (String variable : variables) {
            datos.put(variable, handler.valoresDe(variable));
        }

        String a = variables.get(0);
        String b = variables.size() == 2 ? variables.get(1) : null;
        if (b != null && datos.get(a).size() != datos.get(b).size()) {
            throw new ErrorCalculadora(ErrorCalculadora.LONGITUDES_DISTINTAS,
                    "No se puede calcular la correlación: '" + a + "' tiene " + datos.get(a).size()
                            + " valores y '" + b + "' tiene " + datos.get(b).size() + ".");
        }

        System.out.println("Documento: " + ruta);
        for (Map.Entry<String, List<Double>> entrada : datos.entrySet()) {
            imprimirEstadisticas(entrada.getKey(), Estadisticas.de(entrada.getValue()));
        }
        if (b != null) {
            imprimirCorrelacion(a, b, Estadisticas.correlacion(datos.get(a), datos.get(b)));
        }
    }

    private static void leerDocumento(String ruta, VariablesHandler handler) throws ErrorCalculadora {
        File archivo = new File(ruta);
        if (!archivo.exists()) {
            throw new ErrorCalculadora(ErrorCalculadora.DOCUMENTO_NO_ABIERTO,
                    "No se puede abrir el documento '" + ruta + "': el archivo no existe.");
        }
        if (!archivo.isFile()) {
            throw new ErrorCalculadora(ErrorCalculadora.DOCUMENTO_NO_ABIERTO,
                    "No se puede abrir el documento '" + ruta + "': no es un archivo.");
        }
        if (!archivo.canRead()) {
            throw new ErrorCalculadora(ErrorCalculadora.DOCUMENTO_NO_ABIERTO,
                    "No se puede abrir el documento '" + ruta + "': no hay permiso de lectura.");
        }
        try {
            SAXParserFactory fabrica = SAXParserFactory.newInstance();
            fabrica.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            fabrica.setFeature("http://xml.org/sax/features/external-general-entities", false);
            fabrica.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            fabrica.newSAXParser().parse(archivo, handler);
        } catch (SAXParseException e) {
            throw new ErrorCalculadora(ErrorCalculadora.DOCUMENTO_MAL_FORMADO,
                    "El documento '" + ruta + "' no está bien formado (línea " + e.getLineNumber()
                            + ", columna " + e.getColumnNumber() + "): " + e.getMessage(), e);
        } catch (IOException e) {
            throw new ErrorCalculadora(ErrorCalculadora.DOCUMENTO_NO_ABIERTO,
                    "No se puede leer el documento '" + ruta + "': " + e.getMessage(), e);
        } catch (SAXException | ParserConfigurationException e) {
            throw new ErrorCalculadora(ErrorCalculadora.DOCUMENTO_MAL_FORMADO,
                    "No se pudo procesar el documento '" + ruta + "': " + e.getMessage(), e);
        }
    }

    private static void imprimirEstadisticas(String variable, Estadisticas s) {
        System.out.println();
        System.out.println("Variable: " + variable + " (" + (s.n() == 1 ? "1 valor" : s.n() + " valores") + ")");
        System.out.println("  Media:               " + conDecimales(s.media()));
        System.out.println("  Desviación estándar: " + conDecimales(s.desviacion()));
        System.out.println("  Varianza:            " + conDecimales(s.varianza()));
        System.out.println("  Valor máximo:        " + tal(s.maximo()));
        System.out.println("  Valor mínimo:        " + tal(s.minimo()));
    }

    private static void imprimirCorrelacion(String a, String b, double r) {
        System.out.println();
        String resultado = Double.isNaN(r)
                ? "no definido (una de las variables es constante)"
                : conDecimales(r);
        System.out.println("Coeficiente de correlación (" + a + ", " + b + "): " + resultado);
    }

    private static String conDecimales(double valor) {
        return String.format(Locale.US, "%.4f", valor);
    }

    private static String tal(double valor) {
        return BigDecimal.valueOf(valor).stripTrailingZeros().toPlainString();
    }
}