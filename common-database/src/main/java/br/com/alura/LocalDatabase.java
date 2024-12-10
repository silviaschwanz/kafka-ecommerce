package br.com.alura;

import java.sql.*;

public class LocalDatabase {

    private final Connection connection;

    public LocalDatabase(String name) throws SQLException {
        String url = "jdbc:sqlite:db/" + name + ".db";
        this.connection = DriverManager.getConnection(url);
    }

    public void createIfNotExists(String sql) {
        try {
            connection.createStatement().execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean update(String statement, String... params) {
        try {
            return prepare(statement, params).execute();
        } catch (SQLException e) {
            System.out.println("Erro ao executar o statement:" + statement);
            throw new RuntimeException(e);
        }
    }

    public ResultSet query(String query, String... params) {
        try {
            return prepare(query, params).executeQuery();
        } catch (SQLException e) {
            System.out.println("Erro ao executar a query:" + query);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement prepare(String statement, String[] params) {
        try {
            var prepareStatement = connection.prepareStatement(statement);
            for(int i = 0; i< params.length; i++ ) {
                prepareStatement.setString(i+1, params[i]);
            }
            return prepareStatement;
        } catch (SQLException e) {
            System.out.println("Erro ao tentar preparar sql:" + statement);
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
