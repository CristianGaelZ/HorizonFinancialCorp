/*
 * Clase Consulta
 *
 * Esta clase maneja la generación de recetas médicas y su almacenamiento
 * en la base de datos, incluyendo su registro en el historial médico.
 *
 * usos principales:
 * - Generar una nueva receta para un paciente.
 * - Almacenar la receta en la tabla 'recetas'.
 * - Registrar la receta en la tabla 'historial_medico'.
 * - Manejar errores de base de datos con transacciones.
 *
 * validaciones manejadas:
 * - SQLException en caso de error con la base de datos.
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.*;

