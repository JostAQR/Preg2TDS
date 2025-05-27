package servlet;

import dao.ClienteJpaController;
import dto.Cliente;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 *
 * @author ANDREA
 */
@WebServlet(name = "cambiarContrasena", urlPatterns = {"/contrasena"})
public class cambiarContrasena extends HttpServlet {
     private ClienteJpaController clienteService;
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_Preg2TDS_war_1.0-SNAPSHOTPU");

    @Override
    public void init() throws ServletException {
        super.init();
        clienteService = new ClienteJpaController(emf);
    }

    @Override
    public void destroy() {
        if (emf != null) {
            emf.close();
        }
        super.destroy();
    }

    // CONFIGURAR HEADERS CORS
    private void configurarCORS(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        configurarCORS(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        configurarCORS(response);
        PrintWriter out = response.getWriter();

        try {
            // OBTENER PARÁMETROS
            String login = request.getParameter("login");
            String claveActual = request.getParameter("claveActual");
            String nuevaClave = request.getParameter("nuevaClave");

            // LOGS PARA DEBUGGING
            System.out.println("=== CAMBIO DE CONTRASEÑA ===");
            System.out.println("Login recibido: " + login);
            System.out.println("Clave actual recibida: " + (claveActual != null ? "***" : "null"));
            System.out.println("Nueva clave recibida: " + (nuevaClave != null ? "***" : "null"));
            System.out.println("Content-Type: " + request.getContentType());

            // VALIDAR CAMPOS REQUERIDOS
            if (login == null || login.trim().isEmpty()) {
                System.out.println("ERROR: Login faltante");
                enviarError(out, "El campo login es obligatorio");
                return;
            }

            if (claveActual == null || claveActual.trim().isEmpty()) {
                System.out.println("ERROR: Clave actual faltante");
                enviarError(out, "La contraseña actual es obligatoria");
                return;
            }

            if (nuevaClave == null || nuevaClave.trim().isEmpty()) {
                System.out.println("ERROR: Nueva clave faltante");
                enviarError(out, "La nueva contraseña es obligatoria");
                return;
            }

            // VALIDAR LONGITUD DE NUEVA CONTRASEÑA
            if (nuevaClave.trim().length() < 6) {
                System.out.println("ERROR: Nueva contraseña muy corta");
                enviarError(out, "La nueva contraseña debe tener al menos 6 caracteres");
                return;
            }

            // BUSCAR CLIENTE POR LOGIN
            List<Cliente> clientes = clienteService.findClienteEntities();
            Cliente cliente = null;

            for (Cliente c : clientes) {
                // Usar el método correcto según tu entidad Cliente
                if (c.getLogiClie() != null && c.getLogiClie().equals(login.trim())) {
                    cliente = c;
                    break;
                }
            }

            if (cliente == null) {
                System.out.println("ERROR: Cliente no encontrado - " + login);
                enviarError(out, "Usuario no encontrado");
                return;
            }

            System.out.println("Cliente encontrado: " + cliente.getLogiClie());

            // VERIFICAR CONTRASEÑA ACTUAL
            if (!cliente.getPassClie().equals(claveActual.trim())) {
                System.out.println("ERROR: Contraseña actual incorrecta");
                enviarError(out, "La contraseña actual es incorrecta");
                return;
            }

            // VALIDAR QUE LA NUEVA CONTRASEÑA SEA DIFERENTE
            if (claveActual.trim().equals(nuevaClave.trim())) {
                System.out.println("ERROR: Nueva contraseña igual a la actual");
                enviarError(out, "La nueva contraseña debe ser diferente a la actual");
                return;
            }

            // ACTUALIZAR CONTRASEÑA
            cliente.setPassClie(nuevaClave.trim());
            clienteService.edit(cliente);

            System.out.println("Contraseña actualizada exitosamente para: " + login);

            // ENVIAR RESPUESTA EXITOSA
            JSONObject successJson = new JSONObject();
            successJson.put("success", true);
            successJson.put("message", "Contraseña cambiada exitosamente");
            out.print(successJson.toString());
            out.flush();

        } catch (Exception e) {
            System.out.println("ERROR INTERNO: " + e.getMessage());
            e.printStackTrace();

            JSONObject errorJson = new JSONObject();
            errorJson.put("success", false);
            errorJson.put("message", "Error interno del servidor. Por favor, intente nuevamente.");
            out.print(errorJson.toString());
            out.flush();
        }
    }

    /**
     * Método auxiliar para enviar errores de forma consistente
     */
    private void enviarError(PrintWriter out, String mensaje) {
        JSONObject errorJson = new JSONObject();
        errorJson.put("success", false);
        errorJson.put("message", mensaje);
        out.print(errorJson.toString());
        out.flush();
    }
}
