// 데이터베이스 (mysql)과 java 연결하는 부분 (jdbc 사용)

package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {

    // JDBC URL, 사용자명, 비밀번호
    private static final String URL = "jdbc:mysql://localhost:3306/network_project?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";

    // 데이터베이스 연결 메서드
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
