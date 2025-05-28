/*
 * Clase Cita
 *
 * Esta clase gestiona la creación y cancelación de citas médicas en la base de datos.
 *
 * Usos principales:
 * - Agendar una nueva cita, incluyendo el registro de un paciente si no existe.
 * - Cancelar una cita existente utilizando su ID.
 *
 * Validaciones usadas:
 * - SQLException en caso de error con la base de datos.
 */
import java.sql.*;
import java.util.Scanner;

