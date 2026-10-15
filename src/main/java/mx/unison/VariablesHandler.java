package mx.unison;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VariablesHandler extends DefaultHandler {

    private static final class Elemento {
        final StringBuilder texto = new StringBuilder();
        boolean tieneHijos = false;
    }

    private final Set<String> solicitadas;
    private final Map<String, List<Double>> valores = new LinkedHashMap<>();
    private final Map<String, String> primerInvalido = new LinkedHashMap<>();
    private final Set<String> compuestas = new LinkedHashSet<>();
    private final Set<String> disponibles = new LinkedHashSet<>();
    private final Deque<Elemento> pila = new ArrayDeque<>();
    private Locator locator;

    public VariablesHandler(String... variables) {
        super();
        this.solicitadas = new LinkedHashSet<>(Arrays.asList(variables));
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        valores.clear();
        primerInvalido.clear();
        compuestas.clear();
        disponibles.clear();
        pila.clear();
        for (String variable : solicitadas) {
            valores.put(variable, new ArrayList<>());
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (!pila.isEmpty()) {
            pila.peek().tieneHijos = true;
        }
        pila.push(new Elemento());
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        if (!pila.isEmpty()) {
            pila.peek().texto.append(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        Elemento elemento = pila.pop();
        if (elemento.tieneHijos) {
            if (solicitadas.contains(qName)) {
                compuestas.add(qName);
            }
            return;
        }
        disponibles.add(qName);
        if (solicitadas.contains(qName)) {
            registrar(qName, elemento.texto.toString().trim());
        }
    }

    private void registrar(String variable, String texto) {
        try {
            double valor = new BigDecimal(texto).doubleValue();
            if (!Double.isFinite(valor)) {
                throw new NumberFormatException("fuera de rango");
            }
            valores.get(variable).add(valor);
        } catch (NumberFormatException e) {
            String valorMostrado = texto.isEmpty() ? "vacío" : "'" + texto + "'";
            int linea = locator != null ? locator.getLineNumber() : -1;
            primerInvalido.putIfAbsent(variable,
                    "el valor " + valorMostrado + (linea > 0 ? " (línea " + linea + ")" : "") + " no es un número");
        }
    }

    public List<Double> valoresDe(String variable) throws ErrorCalculadora {
        if (compuestas.contains(variable)) {
            throw new ErrorCalculadora(ErrorCalculadora.VARIABLE_NO_NUMERICA,
                    "'" + variable + "' no es una variable numérica: es un elemento que contiene otros elementos.");
        }
        if (!disponibles.contains(variable)) {
            throw new ErrorCalculadora(ErrorCalculadora.VARIABLE_INEXISTENTE,
                    "La variable '" + variable + "' no existe en el documento." + ayudaNombres(variable));
        }
        if (primerInvalido.containsKey(variable)) {
            throw new ErrorCalculadora(ErrorCalculadora.VARIABLE_NO_NUMERICA,
                    "La variable '" + variable + "' no es numérica: " + primerInvalido.get(variable) + ".");
        }
        return Collections.unmodifiableList(valores.get(variable));
    }

    private String ayudaNombres(String variable) {
        if (disponibles.isEmpty()) {
            return " El documento no contiene variables.";
        }
        StringBuilder ayuda = new StringBuilder();
        for (String nombre : disponibles) {
            if (nombre.equalsIgnoreCase(variable)) {
                ayuda.append(" ¿Quisiste decir '").append(nombre).append("'?");
                break;
            }
        }
        ayuda.append(" Variables disponibles: ").append(String.join(", ", disponibles)).append('.');
        return ayuda.toString();
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        throw e;
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        throw e;
    }
}