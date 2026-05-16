package bdnotas;

import java.sql.*;
import java.util.Scanner;

/**
 * Sistema de Control de Notas - Programación 1
 * Sección A y Sección B
 */
public class BDNotas {

    // ─── Configuración de conexión ───────────────────────────────────────────
    private static final String URL  = "jdbc:mysql://localhost:3306/BDNotas";
    private static final String USER = "root";     
    private static final String PASS = "Exon2312*";       
    static Scanner sc = new Scanner(System.in);

    // ─── Punto de entrada ────────────────────────────────────────────────────
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║  Sistema de Notas - Programación 1       ║");
        System.out.println("╚══════════════════════════════════════════╝");

        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
            System.out.println("✔ Conexión exitosa a BDNotas\n");
            menuPrincipal(con);
        } catch (SQLException e) {
            System.out.println("✘ Error de conexión: " + e.getMessage());
            System.out.println("  Verifique que MySQL esté activo y las credenciales sean correctas.");
        }
    }

    // ─── Menú Principal ──────────────────────────────────────────────────────
    static void menuPrincipal(Connection con) throws SQLException {
        int opcion;
        do {
            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║             MENÚ PRINCIPAL               ║");
            System.out.println("╠══════════════════════════════════════════╣");
            System.out.println("║  1. Ingreso de Alumnos                   ║");
            System.out.println("║  2. Ingreso de Notas                     ║");
            System.out.println("║  3. Eliminar Alumno                      ║");
            System.out.println("║  4. Actualizar datos y notas             ║");
            System.out.println("║  5. Buscar Alumno (Carnet o Nombre)      ║");
            System.out.println("║  6. Obtener Promedios por Sección        ║");
            System.out.println("║  7. Listar Alumnos                       ║");
            System.out.println("║  8. Salir                                ║");
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.print("  Seleccione una opción: ");
            opcion = leerEntero();

            switch (opcion) {
                case 1 -> ingresoAlumno(con);
                case 2 -> ingresoNotas(con);
                case 3 -> eliminarAlumno(con);
                case 4 -> actualizarAlumno(con);
                case 5 -> buscarAlumno(con);
                case 6 -> obtenerPromedios(con);
                case 7 -> listarAlumnos(con);
                case 8 -> System.out.println("\n¡Hasta luego!\n");
                default -> System.out.println("  ⚠ Opción inválida. Intente nuevamente.");
            }
        } while (opcion != 8);
    }

    // ─── Opción 1: Ingreso de Alumno ─────────────────────────────────────────
    static void ingresoAlumno(Connection con) throws SQLException {
        System.out.println("\n── INGRESO DE ALUMNO ──────────────────────");

        System.out.print("  Carnet    : ");
        String carnet = sc.nextLine().trim();
        if (carnet.isEmpty()) { System.out.println("  ⚠ Carnet no puede estar vacío."); return; }

        // Verificar si ya existe
        if (existeAlumno(con, carnet)) {
            System.out.println("  ⚠ Ya existe un alumno con ese carnet.");
            return;
        }

        System.out.print("  Nombres   : ");
        String nombres = sc.nextLine().trim();
        System.out.print("  Apellidos : ");
        String apellidos = sc.nextLine().trim();

        String seccion = pedirSeccion();

        String sql = "INSERT INTO alumnos (carnet, nombres, apellidos, seccion) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, carnet);
            ps.setString(2, nombres);
            ps.setString(3, apellidos);
            ps.setString(4, seccion);
            ps.executeUpdate();
            System.out.println("  ✔ Alumno ingresado exitosamente.");
        }
    }

    // ─── Opción 2: Ingreso de Notas ──────────────────────────────────────────
    static void ingresoNotas(Connection con) throws SQLException {
        System.out.println("\n── INGRESO DE NOTAS ───────────────────────");

        System.out.print("  Carnet del alumno: ");
        String carnet = sc.nextLine().trim();

        if (!existeAlumno(con, carnet)) {
            System.out.println("  ⚠ El alumno no existe en la base de datos.");
            return;
        }

        // Verificar si ya tiene notas
        if (tieneNotas(con, carnet)) {
            System.out.println("  ℹ El alumno ya tiene notas registradas.");
            System.out.print("  ¿Desea sobreescribirlas? (s/n): ");
            if (!sc.nextLine().trim().equalsIgnoreCase("s")) return;

            // Actualizar notas existentes
            System.out.println("  Ingrese las nuevas notas (0 - 100):");
            double n1 = pedirNota("  Nota 1: ");
            double n2 = pedirNota("  Nota 2: ");
            double n3 = pedirNota("  Nota 3: ");

            String sql = "UPDATE notas SET nota1=?, nota2=?, nota3=? WHERE carnet=?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setDouble(1, n1); ps.setDouble(2, n2);
                ps.setDouble(3, n3); ps.setString(4, carnet);
                ps.executeUpdate();
                System.out.printf("  ✔ Notas actualizadas. Promedio: %.2f%n", (n1+n2+n3)/3);
            }
        } else {
            System.out.println("  Ingrese las notas del alumno (0 - 100):");
            double n1 = pedirNota("  Nota 1: ");
            double n2 = pedirNota("  Nota 2: ");
            double n3 = pedirNota("  Nota 3: ");

            String sql = "INSERT INTO notas (carnet, nota1, nota2, nota3) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, carnet);
                ps.setDouble(2, n1); ps.setDouble(3, n2); ps.setDouble(4, n3);
                ps.executeUpdate();
                System.out.printf("  ✔ Notas registradas. Promedio: %.2f%n", (n1+n2+n3)/3);
            }
        }
    }

    // ─── Opción 3: Eliminar Alumno ───────────────────────────────────────────
    static void eliminarAlumno(Connection con) throws SQLException {
        System.out.println("\n── ELIMINAR ALUMNO ────────────────────────");
        System.out.println("  Buscar por: 1) Carnet   2) Nombre");
        System.out.print("  Opción: ");
        int op = leerEntero();

        ResultSet rs = null;
        String carnet = null;

        if (op == 1) {
            System.out.print("  Carnet: ");
            carnet = sc.nextLine().trim();
            rs = buscarPorCarnet(con, carnet);
        } else {
            System.out.print("  Nombre o Apellido: ");
            String nombre = sc.nextLine().trim();
            rs = buscarPorNombre(con, nombre);
        }

        if (rs != null && rs.next()) {
            imprimirFila(rs);
            carnet = rs.getString("carnet");
            System.out.print("\n  ¿Está seguro de eliminar este alumno? (s/n): ");
            if (sc.nextLine().trim().equalsIgnoreCase("s")) {
                String sql = "DELETE FROM alumnos WHERE carnet = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, carnet);
                    ps.executeUpdate();
                    System.out.println("  ✔ Alumno eliminado exitosamente.");
                }
            } else {
                System.out.println("  ℹ Operación cancelada.");
            }
        } else {
            System.out.println("  ⚠ Alumno no encontrado.");
        }
        if (rs != null) rs.close();
    }

    // ─── Opción 4: Actualizar datos y notas ──────────────────────────────────
    static void actualizarAlumno(Connection con) throws SQLException {
        System.out.println("\n── ACTUALIZAR DATOS ───────────────────────");
        System.out.print("  Carnet del alumno a actualizar: ");
        String carnet = sc.nextLine().trim();

        if (!existeAlumno(con, carnet)) {
            System.out.println("  ⚠ El alumno no existe en la base de datos.");
            return;
        }

        System.out.println("  ¿Qué desea actualizar?");
        System.out.println("  1) Nombres    2) Apellidos    3) Sección    4) Notas    5) Todo");
        System.out.print("  Opción: ");
        int op = leerEntero();

        switch (op) {
            case 1 -> {
                System.out.print("  Nuevos nombres: ");
                String nom = sc.nextLine().trim();
                ejecutarUpdate(con, "UPDATE alumnos SET nombres=? WHERE carnet=?", nom, carnet);
            }
            case 2 -> {
                System.out.print("  Nuevos apellidos: ");
                String ape = sc.nextLine().trim();
                ejecutarUpdate(con, "UPDATE alumnos SET apellidos=? WHERE carnet=?", ape, carnet);
            }
            case 3 -> {
                String sec = pedirSeccion();
                ejecutarUpdate(con, "UPDATE alumnos SET seccion=? WHERE carnet=?", sec, carnet);
            }
            case 4 -> actualizarSoloNotas(con, carnet);
            case 5 -> {
                System.out.print("  Nuevos nombres   : "); String nom = sc.nextLine().trim();
                System.out.print("  Nuevos apellidos : "); String ape = sc.nextLine().trim();
                String sec = pedirSeccion();
                String sql = "UPDATE alumnos SET nombres=?, apellidos=?, seccion=? WHERE carnet=?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, nom); ps.setString(2, ape);
                    ps.setString(3, sec); ps.setString(4, carnet);
                    ps.executeUpdate();
                }
                actualizarSoloNotas(con, carnet);
            }
            default -> { System.out.println("  ⚠ Opción inválida."); return; }
        }
        System.out.println("  ✔ Datos actualizados exitosamente.");
    }

    static void actualizarSoloNotas(Connection con, String carnet) throws SQLException {
        double n1 = pedirNota("  Nueva Nota 1: ");
        double n2 = pedirNota("  Nueva Nota 2: ");
        double n3 = pedirNota("  Nueva Nota 3: ");
        if (tieneNotas(con, carnet)) {
            String sql = "UPDATE notas SET nota1=?, nota2=?, nota3=? WHERE carnet=?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setDouble(1,n1); ps.setDouble(2,n2); ps.setDouble(3,n3); ps.setString(4,carnet);
                ps.executeUpdate();
            }
        } else {
            String sql = "INSERT INTO notas (carnet, nota1, nota2, nota3) VALUES (?,?,?,?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1,carnet); ps.setDouble(2,n1); ps.setDouble(3,n2); ps.setDouble(4,n3);
                ps.executeUpdate();
            }
        }
    }

    // ─── Opción 5: Buscar Alumno ─────────────────────────────────────────────
    static void buscarAlumno(Connection con) throws SQLException {
        System.out.println("\n── BUSCAR ALUMNO ──────────────────────────");
        System.out.println("  1) Buscar por Carnet    2) Buscar por Nombre");
        System.out.print("  Opción: ");
        int op = leerEntero();

        ResultSet rs;
        if (op == 1) {
            System.out.print("  Carnet: ");
            rs = buscarPorCarnet(con, sc.nextLine().trim());
        } else {
            System.out.print("  Nombre o Apellido: ");
            rs = buscarPorNombre(con, sc.nextLine().trim());
        }

        imprimirEncabezado();
        boolean encontrado = false;
        while (rs.next()) {
            imprimirFila(rs);
            encontrado = true;
        }
        if (!encontrado) System.out.println("  ⚠ No se encontraron resultados.");
        rs.close();
    }

    // ─── Opción 6: Promedios por Sección ─────────────────────────────────────
    static void obtenerPromedios(Connection con) throws SQLException {
        System.out.println("\n── PROMEDIOS POR SECCIÓN ──────────────────");

        String sql = """
            SELECT a.seccion,
                   COUNT(a.carnet) AS total_alumnos,
                   AVG(n.promedio) AS promedio_seccion,
                   MIN(n.promedio) AS nota_minima,
                   MAX(n.promedio) AS nota_maxima
            FROM alumnos a
            LEFT JOIN notas n ON a.carnet = n.carnet
            GROUP BY a.seccion
            ORDER BY a.seccion
            """;

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.println("  ┌──────────┬───────────┬──────────────────┬───────────┬───────────┐");
            System.out.println("  │ Sección  │  Alumnos  │ Promedio Sección │  Mínima   │  Máxima   │");
            System.out.println("  ├──────────┼───────────┼──────────────────┼───────────┼───────────┤");

            boolean hayDatos = false;
            while (rs.next()) {
                hayDatos = true;
                System.out.printf("  │ Secc. %-2s │    %-5d  │     %8.2f     │   %6.2f  │   %6.2f  │%n",
                    rs.getString("seccion"),
                    rs.getInt("total_alumnos"),
                    rs.getDouble("promedio_seccion"),
                    rs.getDouble("nota_minima"),
                    rs.getDouble("nota_maxima"));
            }
            System.out.println("  └──────────┴───────────┴──────────────────┴───────────┴───────────┘");
            if (!hayDatos) System.out.println("  ⚠ No hay datos registrados.");
        }
    }

    // ─── Opción 7: Listar Alumnos ────────────────────────────────────────────
    static void listarAlumnos(Connection con) throws SQLException {
        System.out.println("\n── LISTAR ALUMNOS ─────────────────────────");
        String seccion = pedirSeccion();

        System.out.println("  Ordenar por:");
        System.out.println("  1) Carnet   2) Nombres   3) Apellidos   4) Nota");
        System.out.print("  Opción (Enter = sin orden especial): ");
        String opStr = sc.nextLine().trim();

        String orden = switch (opStr) {
            case "1" -> "a.carnet";
            case "2" -> "a.nombres";
            case "3" -> "a.apellidos";
            case "4" -> "n.promedio DESC";
            default  -> "a.carnet";
        };

        String sql = """
            SELECT a.carnet, a.nombres, a.apellidos, a.seccion,
                   COALESCE(n.nota1, 0) AS nota1,
                   COALESCE(n.nota2, 0) AS nota2,
                   COALESCE(n.nota3, 0) AS nota3,
                   COALESCE(n.promedio, 0) AS promedio
            FROM alumnos a
            LEFT JOIN notas n ON a.carnet = n.carnet
            WHERE a.seccion = ?
            ORDER BY """ + orden;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, seccion);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n  ╔══ Programación 1 - Sección " + seccion + " ══════════════════════════════════════════╗");
            System.out.println("  ║ Carnet     │ Nombres             │ Apellidos           │ N1    N2    N3   │ Prom  ║");
            System.out.println("  ╠════════════╪═════════════════════╪═════════════════════╪══════════════════╪═══════╣");

            int contador = 0;
            while (rs.next()) {
                System.out.printf("  ║ %-10s │ %-19s │ %-19s │ %5.1f%5.1f%5.1f │ %5.2f ║%n",
                    rs.getString("carnet"),
                    truncar(rs.getString("nombres"), 19),
                    truncar(rs.getString("apellidos"), 19),
                    rs.getDouble("nota1"),
                    rs.getDouble("nota2"),
                    rs.getDouble("nota3"),
                    rs.getDouble("promedio"));
                contador++;
            }
            System.out.println("  ╚════════════╧═════════════════════╧═════════════════════╧══════════════════╧═══════╝");
            System.out.println("  Total de alumnos en sección " + seccion + ": " + contador);
            rs.close();
        }
    }

    // ─── Métodos auxiliares ──────────────────────────────────────────────────

    static boolean existeAlumno(Connection con, String carnet) throws SQLException {
        String sql = "SELECT 1 FROM alumnos WHERE carnet = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, carnet);
            ResultSet rs = ps.executeQuery();
            boolean existe = rs.next();
            rs.close();
            return existe;
        }
    }

    static boolean tieneNotas(Connection con, String carnet) throws SQLException {
        String sql = "SELECT 1 FROM notas WHERE carnet = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, carnet);
            ResultSet rs = ps.executeQuery();
            boolean tiene = rs.next();
            rs.close();
            return tiene;
        }
    }

    static ResultSet buscarPorCarnet(Connection con, String carnet) throws SQLException {
        String sql = """
            SELECT a.carnet, a.nombres, a.apellidos, a.seccion,
                   COALESCE(n.nota1,0) nota1, COALESCE(n.nota2,0) nota2,
                   COALESCE(n.nota3,0) nota3, COALESCE(n.promedio,0) promedio
            FROM alumnos a LEFT JOIN notas n ON a.carnet=n.carnet
            WHERE a.carnet = ?
            """;
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, carnet);
        return ps.executeQuery();
    }

    static ResultSet buscarPorNombre(Connection con, String nombre) throws SQLException {
        String sql = """
            SELECT a.carnet, a.nombres, a.apellidos, a.seccion,
                   COALESCE(n.nota1,0) nota1, COALESCE(n.nota2,0) nota2,
                   COALESCE(n.nota3,0) nota3, COALESCE(n.promedio,0) promedio
            FROM alumnos a LEFT JOIN notas n ON a.carnet=n.carnet
            WHERE a.nombres LIKE ? OR a.apellidos LIKE ?
            """;
        PreparedStatement ps = con.prepareStatement(sql);
        String patron = "%" + nombre + "%";
        ps.setString(1, patron);
        ps.setString(2, patron);
        return ps.executeQuery();
    }

    static void ejecutarUpdate(Connection con, String sql, String val, String carnet)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, val);
            ps.setString(2, carnet);
            ps.executeUpdate();
        }
    }

    static void imprimirEncabezado() {
        System.out.println("\n  ┌────────────┬─────────────────────┬─────────────────────┬─────────┬────────────────────┬────────┐");
        System.out.println("  │ Carnet     │ Nombres             │ Apellidos           │ Sección │ Notas (N1,N2,N3)   │  Prom  │");
        System.out.println("  ├────────────┼─────────────────────┼─────────────────────┼─────────┼────────────────────┼────────┤");
    }

    static void imprimirFila(ResultSet rs) throws SQLException {
        System.out.printf("  │ %-10s │ %-19s │ %-19s │   %-5s │ %5.1f, %5.1f, %5.1f │ %5.2f │%n",
            rs.getString("carnet"),
            truncar(rs.getString("nombres"), 19),
            truncar(rs.getString("apellidos"), 19),
            rs.getString("seccion"),
            rs.getDouble("nota1"),
            rs.getDouble("nota2"),
            rs.getDouble("nota3"),
            rs.getDouble("promedio"));
    }

    static String pedirSeccion() {
        String sec;
        do {
            System.out.print("  Sección (A/B): ");
            sec = sc.nextLine().trim().toUpperCase();
        } while (!sec.equals("A") && !sec.equals("B"));
        return sec;
    }

    static double pedirNota(String mensaje) {
        double nota;
        do {
            System.out.print(mensaje);
            try { nota = Double.parseDouble(sc.nextLine().trim()); }
            catch (NumberFormatException e) { nota = -1; }
            if (nota < 0 || nota > 100)
                System.out.println("  ⚠ La nota debe estar entre 0 y 100.");
        } while (nota < 0 || nota > 100);
        return nota;
    }

    static int leerEntero() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    static String truncar(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}
