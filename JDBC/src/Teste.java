
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Teste {

    private static final String URL = "jdbc:postgresql://aws-0-us-west-2.pooler.supabase.com:6543/postgres";
    private static final String USERNAME = "postgres.eqsisjbhqvugapwektid";
    private static final String PASSWORD = "n1AL7Kc7lZrJMX0I";

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static void main(String[] args) {
        System.out.println("=== BIBLIOTECA DIGITAL - LAB6 ===");

        // 1. INSERIR LIVROS E EMPRÉSTIMOS (Create)
        System.out.println("\n--- 1. Inserindo livros e empréstimos ---");
        long idLivro1 = inserirLivro("O Senhor dos Anéis", "J.R.R. Tolkien");
        long idLivro2 = inserirLivro("Dom Casmurro", "Machado de Assis");

        if (idLivro1 != -1) {
            inserirEmprestimo(idLivro1, Date.valueOf("2026-03-01"));
            inserirEmprestimo(idLivro1, Date.valueOf("2026-03-15"));
        }
        if (idLivro2 != -1) {
            inserirEmprestimo(idLivro2, Date.valueOf("2026-03-10"));
        }

        // 2. LISTAR REGISTROS (Read)
        System.out.println("\n--- 2. Listando livros e empréstimos armazenados ---");
        listarLivrosComEmprestimos();

        // 3. ATUALIZAR REGISTRO (Update)
        System.out.println("\n--- 3. Atualizando título do Livro ---");
        if (idLivro1 != -1) {
            atualizarLivro(idLivro1, "O Senhor dos Anéis: A Sociedade do Anel", "J.R.R. Tolkien");
        }

        System.out.println("\n--- Listagem após atualização ---");
        listarLivrosComEmprestimos();

        // 4. REMOVER REGISTRO (Delete)
        System.out.println("\n--- 4. Removendo um empréstimo ---");
        removerEmprestimo(1L);

        System.out.println("\n--- Listagem final ---");
        listarLivrosComEmprestimos();
    }

    // ==========================================
    // OPERAÇÕES DE INSERÇÃO (CREATE)
    // ==========================================

    public static long inserirLivro(String titulo, String autor) {
        // Apontando para a tabela 'livros'
        String sql = "INSERT INTO livros (titulo, autor) VALUES (?, ?) RETURNING id";

        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, titulo);
            pstmt.setString(2, autor);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                long idGerado = rs.getLong(1);
                System.out.println("Livro cadastrado com sucesso! ID: " + idGerado);
                return idGerado;
            }

        } catch (SQLException e) {
            System.err.println("Erro ao inserir livro: " + e.getMessage());
        }
        return -1;
    }

    public static void inserirEmprestimo(long livroId, Date dataRetirada) {
        // Apontando para a tabela 'emprestimo'
        String sql = "INSERT INTO emprestimo (livro_id, data_retirada) VALUES (?, ?)";

        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, livroId);
            pstmt.setDate(2, dataRetirada);

            pstmt.executeUpdate();
            System.out.println("Empréstimo registrado para o livro ID " + livroId);

        } catch (SQLException e) {
            System.err.println("Erro ao registrar empréstimo: " + e.getMessage());
        }
    }

    // ==========================================
    // OPERAÇÃO DE LISTAGEM (READ)
    // ==========================================

    public static void listarLivrosComEmprestimos() {
        String sql = "SELECT l.id AS livro_id, l.titulo, l.autor, e.id AS emprestimo_id, e.data_retirada " +
                     "FROM livros l LEFT JOIN emprestimo e ON l.id = e.livro_id " +
                     "ORDER BY l.id, e.data_retirada";

        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("--------------------------------------------------");
            while (rs.next()) {
                long idLivro = rs.getLong("livro_id");
                String titulo = rs.getString("titulo");
                String autor = rs.getString("autor");
                long idEmp = rs.getLong("emprestimo_id");
                Date dataRetirada = rs.getDate("data_retirada");

                System.out.println("Livro [" + idLivro + "]: " + titulo + " | Autor: " + autor);
                if (idEmp > 0) {
                    System.out.println("   └─ Empréstimo ID: " + idEmp + " | Data Retirada: " + dataRetirada);
                } else {
                    System.out.println("   └─ (Sem empréstimos registrados)");
                }
            }
            System.out.println("--------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("Erro ao listar dados: " + e.getMessage());
        }
    }

    // ==========================================
    // OPERAÇÃO DE ATUALIZAÇÃO (UPDATE)
    // ==========================================

    public static void atualizarLivro(long id, String novoTitulo, String novoAutor) {
        String sql = "UPDATE livros SET titulo = ?, autor = ? WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, novoTitulo);
            pstmt.setString(2, novoAutor);
            pstmt.setLong(3, id);

            int linhas = pstmt.executeUpdate();
            if (linhas > 0) {
                System.out.println("Livro ID " + id + " atualizado com sucesso!");
            } else {
                System.out.println("Nenhum livro encontrado com ID " + id);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar livro: " + e.getMessage());
        }
    }

    // ==========================================
    // OPERAÇÃO DE REMOÇÃO (DELETE)
    // ==========================================

    public static void removerEmprestimo(long idEmprestimo) {
        String sql = "DELETE FROM emprestimo WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, idEmprestimo);

            int linhas = pstmt.executeUpdate();
            if (linhas > 0) {
                System.out.println("Empréstimo ID " + idEmprestimo + " removido com sucesso!");
            } else {
                System.out.println("Nenhum empréstimo encontrado com ID " + idEmprestimo);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao remover empréstimo: " + e.getMessage());
        }
    }
}