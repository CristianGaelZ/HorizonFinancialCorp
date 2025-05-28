import java.sql.*;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.ResultSet;

class Usuario {
    private String nombre;
    private String rol;

    public Usuario(String nombre, String rol) {
        this.nombre = nombre;
        this.rol = rol;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRol() {
        return rol;
    }

    public static Usuario login(String usuario, String password) {
        String query = "SELECT nombre, rol FROM usuarios WHERE nombre = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, usuario);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Usuario(rs.getString("nombre"), rs.getString("rol"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}


class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/consultorio";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Error al conectar con la base de datos: " + e.getMessage());
            return null;
        }
    }
}

class consulta {
    /**
     * Método para generar una receta médica y guardarla en el historial médico del paciente.
     *
     * @param scanner Scanner para la entrada de datos.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    public static void generarReceta(Scanner scanner) throws SQLException {
        System.out.print("Nombre del paciente: ");
        String nombre = scanner.next();
        scanner.nextLine();
        System.out.print("Receta médica: ");
        String receta = scanner.nextLine();

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Iniciar transacción

            // Insertar la receta en la base de datos
            String insertRecetaQuery = "INSERT INTO recetas (paciente, receta) VALUES (?, ?)";
            try (PreparedStatement stmtReceta = conn.prepareStatement(insertRecetaQuery, Statement.RETURN_GENERATED_KEYS)) {
                stmtReceta.setString(1, nombre);
                stmtReceta.setString(2, receta);
                stmtReceta.executeUpdate();

                ResultSet rs = stmtReceta.getGeneratedKeys();
                if (rs.next()) {
                    int recetaId = rs.getInt(1);

                    // Insertar la receta en el historial médico del paciente
                    String insertHistorialQuery = "INSERT INTO historial_medico (paciente, receta_id, fecha, receta) VALUES (?, ?, NOW(), ?)";
                    try (PreparedStatement stmtHistorial = conn.prepareStatement(insertHistorialQuery)) {
                        stmtHistorial.setString(1, nombre);
                        stmtHistorial.setInt(2, recetaId);
                        stmtHistorial.setString(3, receta);
                        stmtHistorial.executeUpdate();
                    }
                }

                conn.commit(); // Confirmar la transacción
                System.out.println("Receta generada y guardada en el historial.");
            } catch (SQLException e) {
                conn.rollback(); // Revertir en caso de error
                System.out.println("Error al generar la receta: " + e.getMessage());
            }
        }
    }
}


class Cita {
    /*
     * agendar cita medica
     */
    public static void agendarCita(Scanner scanner) throws SQLException {
        System.out.print("Nombre del paciente: ");
        String nombre = scanner.next();
        System.out.print("Edad: ");
        int edad = scanner.nextInt();
        System.out.print("Teléfono: ");
        String telefono = scanner.next();
        scanner.nextLine();
        System.out.print("Fecha y hora (2005-06-10 10:56): ");
        String fechaHora = scanner.nextLine();
        System.out.print("Caracteristicas: ");
        String descripcion = scanner.nextLine();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Insertar el paciente en la base de datos
            String pacienteQuery = "INSERT INTO pacientes (nombre, edad, telefono) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(pacienteQuery, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, nombre);
                stmt.setInt(2, edad);
                stmt.setString(3, telefono);
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int pacienteId = rs.getInt(1);
                        // Insertar la cita médica vinculada al paciente
                        String citaQuery = "INSERT INTO citas (paciente_id, fecha_hora, descripcion) VALUES (?, ?, ?)";
                        try (PreparedStatement stmtCita = conn.prepareStatement(citaQuery)) {
                            stmtCita.setInt(1, pacienteId);
                            stmtCita.setString(2, fechaHora);
                            stmtCita.setString(3, descripcion);
                            stmtCita.executeUpdate();
                            System.out.println("Cita agendada exitosamente.");
                        }
                    } else {
                        System.out.println("Error: No se pudo obtener el ID del paciente.");
                    }
                }
            }
        }
    }

    /**
     * Método para cancelar una cita médica.
     *
     * @param scanner Scanner para la entrada de datos.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    public static void cancelarCita(Scanner scanner) throws SQLException {
        System.out.print("ID de la cita a cancelar: ");
        int citaId = scanner.nextInt();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "DELETE FROM citas WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, citaId);
                int filas = stmt.executeUpdate();
                System.out.println(filas > 0 ? "Cita cancelada." : "Cita no encontrada.");
            }
        }
    }
}


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Solicitar credenciales del usuario
        System.out.print("Usuario: ");
        String usuario = scanner.next();
        System.out.print("Contraseña: ");
        String password = scanner.next();

        // Intentar iniciar sesión con las credenciales ingresadas
        Usuario user = Usuario.login(usuario, password);
        if (user == null) {
            System.out.println("Error, ingrese correctamente.");
            return;
        }

        System.out.println("Hola, " + user.getNombre());

        // Bucle del menú de opciones
        while (true) {
            System.out.println("1. Agendar Cita");
            System.out.println("2. Cancelar Cita");
            System.out.println("3. Generar Receta (Solo el medico puede)");
            System.out.println("4. Salir");
            System.out.print("Seleccione una opción: ");

            int opcion = scanner.nextInt();
            try {
                switch (opcion) {
                    case 1:
                        Cita.agendarCita(scanner);
                        break;
                    case 2:
                        Cita.cancelarCita(scanner);
                        break;
                    case 3:
                        // Valida si el usuario es un doctor antes de permitir la generación de receta
                        if (user.getRol().equals("doctor")) {
                            consulta.generarReceta(scanner);
                        } else {
                            System.out.println("Acceso no autorizado.");
                        }
                        break;
                    case 4:
                        System.out.println("Bonito día...");
                        return;
                    default:
                        System.out.println("Opción no válida.");
                }
            } catch (SQLException e) {
                System.out.println("Error en la base de datos: " + e.getMessage());
            }
        }
    }
}
