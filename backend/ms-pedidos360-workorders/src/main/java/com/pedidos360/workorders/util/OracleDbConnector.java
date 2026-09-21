package com.pedidos360.workorders.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Utilidad de diagnóstico para validar la conexión a Oracle Cloud Database.
 * No participa del arranque de Spring Boot; se ejecuta manualmente con:
 * <pre>java -cp "target/classes;%USERPROFILE%\.m2\repository\com\oracle\database\jdbc\ojdbc11\23.3.0.23.09\ojdbc11-23.3.0.23.09.jar" com.pedidos360.workorders.util.OracleDbConnector</pre>
 * Las credenciales se leen de variables de entorno (ver .env.example).
 */
public class OracleDbConnector {

    // ============================================================
    //  REEMPLAZA TUS CREDENCIALES AQUÍ (o mejor: variables de entorno)
    // ============================================================
    private static final String ENV_USER     = "ORACLE_USERNAME";
    private static final String ENV_PASSWORD = "ORACLE_PASSWORD";
    private static final String ENV_URL      = "ORACLE_JDBC_URL";
    private static final String ENV_WALLET   = "ORACLE_WALLET_LOCATION"; // carpeta del wallet (TNS_ADMIN)
    private static final String ENV_ALIAS    = "ORACLE_TNS_ALIAS";

    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            testConnection(conn);
        } catch (Exception e) {
            System.err.println("[ERROR] No se pudo conectar a Oracle: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static Connection getConnection() throws SQLException {
        String user     = env(ENV_USER, "ADMIN");           // <-- REEMPLAZA por tu usuario
        String password = env(ENV_PASSWORD, "TU_PASSWORD"); // <-- REEMPLAZA por tu password
        String url      = env(ENV_URL, null);
        String wallet   = env(ENV_WALLET, null);
        String alias    = env(ENV_ALIAS, "pedidos360_high");

        if (url == null || url.isBlank()) {
            url = buildJdbcUrl(wallet, alias);
        }

        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("oracle.net.ssl_server_dn_match", "true"); // TLS: valida el certificado

        try {
            Class.forName("oracle.jdbc.OracleDriver");
            System.out.println("[INFO] Conectando a: " + url);
            return DriverManager.getConnection(url, props);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver Oracle (ojdbc11) no encontrado en el classpath.", e);
        } catch (SQLException e) {
            throw classify(e);
        }
    }

    private static String buildJdbcUrl(String wallet, String alias) {
        if (wallet != null && !wallet.isBlank()) {
            // ---------- MODO WALLET (OCI Autonomous / TLS) ----------
            return "jdbc:oracle:thin:@" + alias + "?TNS_ADMIN=" + wallet;
        }
        // ---------- MODO DIRECTO (sin TLS) ----------
        // REEMPLAZA host / puerto / service_name
        return "jdbc:oracle:thin:@//localhost:1521/FREEPDB1";
    }

    private static void testConnection(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT 1 FROM DUAL")) {
            if (rs.next()) {
                System.out.println("[OK] Conexión exitosa. SELECT 1 => " + rs.getInt(1));
            }
        }
    }

    private static SQLException classify(SQLException e) {
        String msg;
        switch (e.getErrorCode()) {
            case 1017:  msg = "ORA-01017: usuario/contraseña inválidos."; break;
            case 12514: msg = "ORA-12514: el servicio no existe (revisa SERVICE_NAME o alias)."; break;
            case 12154: msg = "ORA-12154: no se pudo resolver el TNS. Verifica TNS_ADMIN y el alias."; break;
            case 12505: msg = "ORA-12505: el listener no conoce el SID/service."; break;
            case 12543: msg = "ORA-12543: host inalcanzable (red/firewall/endpoint)."; break;
            case 17002: msg = "ORA-17002: timeout de red."; break;
            case 29024: msg = "ORA-29024: falló el handshake TLS del Wallet (certificados)."; break;
            default:    msg = "Error Oracle (ORA-" + e.getErrorCode() + "): " + e.getMessage();
        }
        return new SQLException(msg, e);
    }

    private static String env(String key, String defaultValue) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? defaultValue : v;
    }
}
