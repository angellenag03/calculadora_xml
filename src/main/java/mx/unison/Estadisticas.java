package mx.unison;

import java.util.List;

public record Estadisticas(int n, double media, double varianza, double desviacion,
                           double maximo, double minimo) {

    public static Estadisticas de(List<Double> datos) {
        if (datos.isEmpty()) {
            throw new IllegalArgumentException("Se requiere al menos un valor");
        }
        int n = datos.size();
        double media = media(datos);
        double maximo = datos.get(0);
        double minimo = datos.get(0);
        double sumaCuadrados = 0;
        for (double d : datos) {
            double diferencia = d - media;
            sumaCuadrados += diferencia * diferencia;
            maximo = Math.max(maximo, d);
            minimo = Math.min(minimo, d);
        }
        double varianza = sumaCuadrados / n;
        return new Estadisticas(n, media, varianza, Math.sqrt(varianza), maximo, minimo);
    }

    public static double correlacion(List<Double> x, List<Double> y) {
        if (x.size() != y.size() || x.isEmpty()) {
            throw new IllegalArgumentException("Las variables deben tener la misma cantidad de valores (al menos uno)");
        }
        if (esConstante(x) || esConstante(y)) {
            return Double.NaN;
        }
        double mediaX = media(x);
        double mediaY = media(y);
        double sxy = 0, sxx = 0, syy = 0;
        for (int i = 0; i < x.size(); i++) {
            double dx = x.get(i) - mediaX;
            double dy = y.get(i) - mediaY;
            sxy += dx * dy;
            sxx += dx * dx;
            syy += dy * dy;
        }
        double r = sxy / (Math.sqrt(sxx) * Math.sqrt(syy));
        return Math.max(-1.0, Math.min(1.0, r)); 
    }

    private static double media(List<Double> datos) {
        double suma = 0;
        for (double d : datos) {
            suma += d;
        }
        return suma / datos.size();
    }

    private static boolean esConstante(List<Double> datos) {
        double primero = datos.get(0);
        for (double d : datos) {
            if (d != primero) {
                return false;
            }
        }
        return true;
    }
}