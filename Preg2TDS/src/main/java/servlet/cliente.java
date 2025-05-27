package servlet;

import dto.Cliente;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author ANDREA
 */
@WebServlet(name = "estudiante", urlPatterns = {"/estudiante"})
public class cliente extends HttpServlet {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private EntityManagerFactory emf;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            emf = Persistence.createEntityManagerFactory("com.mycompany_Preg2TDS_war_1.0-SNAPSHOTPU");
        } catch (Exception e) {
            throw new ServletException("Error al inicializar EntityManagerFactory", e);
        }
    }

    @Override
    public void destroy() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        super.destroy();
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    private void configurarCORS(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Max-Age", "3600");
    }

    // Manejar peticiones OPTIONS para CORS
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        configurarCORS(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // GET - Listar clientes
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        configurarCORS(response);
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        EntityManager em = null;
        JSONObject jsonResponse = new JSONObject();

        try {
            em = getEntityManager();
            List<Cliente> clientes = em.createQuery("SELECT c FROM Cliente c ORDER BY c.codiClie DESC", Cliente.class).getResultList();
            JSONArray jsonArray = new JSONArray();

            for (Cliente c : clientes) {
                JSONObject obj = new JSONObject();
                obj.put("codiClie", c.getCodiClie());
                obj.put("ndniClie", c.getNdniClie() != null ? c.getNdniClie() : "");
                obj.put("appaClie", c.getAppaClie() != null ? c.getAppaClie() : "");
                obj.put("apmaClie", c.getApmaClie() != null ? c.getApmaClie() : "");
                obj.put("nombClie", c.getNombClie() != null ? c.getNombClie() : "");
                obj.put("fechNaciClie", c.getFechNaciClie() != null ? dateFormat.format(c.getFechNaciClie()) : "");
                obj.put("logiClie", c.getLogiClie() != null ? c.getLogiClie() : "");
                obj.put("passClie", c.getPassClie() != null ? c.getPassClie() : "");

                jsonArray.put(obj);
            }

            jsonResponse.put("success", true);
            jsonResponse.put("data", jsonArray);
            jsonResponse.put("message", "Clientes cargados correctamente");
            jsonResponse.put("count", clientes.size());

        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Error al cargar clientes: " + e.getMessage());
            jsonResponse.put("error", e.getClass().getSimpleName());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        PrintWriter out = response.getWriter();
        out.print(jsonResponse.toString());
        out.flush();
    }

    // POST - Crear cliente
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        configurarCORS(response);
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        EntityManager em = null;
        JSONObject jsonResponse = new JSONObject();

        try {
            // Leer datos JSON del request
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            try (BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
            }

            if (jsonBuilder.length() == 0) {
                throw new Exception("No se recibieron datos JSON");
            }

            JSONObject json = new JSONObject(jsonBuilder.toString());

            // Validar campos requeridos
            if (!json.has("ndniClie") || json.getString("ndniClie").trim().isEmpty()) {
                throw new Exception("El DNI es requerido");
            }
            if (!json.has("appaClie") || json.getString("appaClie").trim().isEmpty()) {
                throw new Exception("El apellido paterno es requerido");
            }
            if (!json.has("apmaClie") || json.getString("apmaClie").trim().isEmpty()) {
                throw new Exception("El apellido materno es requerido");
            }
            if (!json.has("nombClie") || json.getString("nombClie").trim().isEmpty()) {
                throw new Exception("El nombre es requerido");
            }
            if (!json.has("fechNaciClie") || json.getString("fechNaciClie").trim().isEmpty()) {
                throw new Exception("La fecha de nacimiento es requerida");
            }
            if (!json.has("logiClie") || json.getString("logiClie").trim().isEmpty()) {
                throw new Exception("El login es requerido");
            }
            if (!json.has("passClie") || json.getString("passClie").trim().isEmpty()) {
                throw new Exception("La contraseña es requerida");
            }

            em = getEntityManager();

            // Verificar si ya existe un cliente con el mismo DNI
            List<Cliente> clientesExistentes = em.createQuery(
                "SELECT c FROM Cliente c WHERE c.ndniClie = :dni", Cliente.class)
                .setParameter("dni", json.getString("ndniClie").trim())
                .getResultList();
            
            if (!clientesExistentes.isEmpty()) {
                throw new Exception("Ya existe un cliente con el DNI: " + json.getString("ndniClie"));
            }

            // Verificar si ya existe un cliente con el mismo login
            List<Cliente> loginExistentes = em.createQuery(
                "SELECT c FROM Cliente c WHERE c.logiClie = :login", Cliente.class)
                .setParameter("login", json.getString("logiClie").trim())
                .getResultList();
            
            if (!loginExistentes.isEmpty()) {
                throw new Exception("Ya existe un cliente con el login: " + json.getString("logiClie"));
            }

            // Crear nuevo cliente
            Cliente cliente = new Cliente();
            cliente.setNdniClie(json.getString("ndniClie").trim());
            cliente.setAppaClie(json.getString("appaClie").trim().toUpperCase());
            cliente.setApmaClie(json.getString("apmaClie").trim().toUpperCase());
            cliente.setNombClie(json.getString("nombClie").trim().toUpperCase());
            
            // Parsear fecha
            try {
                Date fechaNacimiento = dateFormat.parse(json.getString("fechNaciClie"));
                cliente.setFechNaciClie(fechaNacimiento);
            } catch (ParseException e) {
                throw new Exception("Formato de fecha inválido. Use yyyy-MM-dd");
            }
            
            cliente.setLogiClie(json.getString("logiClie").trim());
            cliente.setPassClie(json.getString("passClie"));

            // Guardar en base de datos
            em.getTransaction().begin();
            em.persist(cliente);
            em.getTransaction().commit();

            jsonResponse.put("success", true);
            jsonResponse.put("message", "Cliente creado correctamente");
            jsonResponse.put("id", cliente.getCodiClie());
            response.setStatus(HttpServletResponse.SC_CREATED);

        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Error al crear cliente: " + e.getMessage());
            jsonResponse.put("error", e.getClass().getSimpleName());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        PrintWriter out = response.getWriter();
        out.print(jsonResponse.toString());
        out.flush();
    }

    // PUT - Actualizar cliente
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        configurarCORS(response);
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        EntityManager em = null;
        JSONObject jsonResponse = new JSONObject();

        try {
            // Leer datos JSON del request
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            try (BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
            }

            if (jsonBuilder.length() == 0) {
                throw new Exception("No se recibieron datos JSON");
            }

            JSONObject json = new JSONObject(jsonBuilder.toString());

            // Validar que se incluya el ID
            if (!json.has("codiClie")) {
                throw new Exception("ID del cliente es requerido para actualización");
            }

            int codiClie = json.getInt("codiClie");
            em = getEntityManager();

            em.getTransaction().begin();
            Cliente cliente = em.find(Cliente.class, codiClie);

            if (cliente == null) {
                em.getTransaction().rollback();
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Cliente no encontrado con ID: " + codiClie);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } else {
                // Verificar si el DNI ya existe en otro cliente
                if (json.has("ndniClie") && !json.getString("ndniClie").trim().isEmpty()) {
                    List<Cliente> clientesConDNI = em.createQuery(
                        "SELECT c FROM Cliente c WHERE c.ndniClie = :dni AND c.codiClie != :id", Cliente.class)
                        .setParameter("dni", json.getString("ndniClie").trim())
                        .setParameter("id", codiClie)
                        .getResultList();
                    
                    if (!clientesConDNI.isEmpty()) {
                        throw new Exception("Ya existe otro cliente con el DNI: " + json.getString("ndniClie"));
                    }
                }

                // Verificar si el login ya existe en otro cliente
                if (json.has("logiClie") && !json.getString("logiClie").trim().isEmpty()) {
                    List<Cliente> clientesConLogin = em.createQuery(
                        "SELECT c FROM Cliente c WHERE c.logiClie = :login AND c.codiClie != :id", Cliente.class)
                        .setParameter("login", json.getString("logiClie").trim())
                        .setParameter("id", codiClie)
                        .getResultList();
                    
                    if (!clientesConLogin.isEmpty()) {
                        throw new Exception("Ya existe otro cliente con el login: " + json.getString("logiClie"));
                    }
                }

                // Actualizar campos
                if (json.has("ndniClie")) {
                    cliente.setNdniClie(json.getString("ndniClie").trim());
                }
                if (json.has("appaClie")) {
                    cliente.setAppaClie(json.getString("appaClie").trim().toUpperCase());
                }
                if (json.has("apmaClie")) {
                    cliente.setApmaClie(json.getString("apmaClie").trim().toUpperCase());
                }
                if (json.has("nombClie")) {
                    cliente.setNombClie(json.getString("nombClie").trim().toUpperCase());
                }
                if (json.has("fechNaciClie") && !json.getString("fechNaciClie").trim().isEmpty()) {
                    try {
                        Date fechaNacimiento = dateFormat.parse(json.getString("fechNaciClie"));
                        cliente.setFechNaciClie(fechaNacimiento);
                    } catch (ParseException e) {
                        throw new Exception("Formato de fecha inválido. Use yyyy-MM-dd");
                    }
                }
                if (json.has("logiClie")) {
                    cliente.setLogiClie(json.getString("logiClie").trim());
                }
                if (json.has("passClie")) {
                    cliente.setPassClie(json.getString("passClie"));
                }

                em.merge(cliente);
                em.getTransaction().commit();

                jsonResponse.put("success", true);
                jsonResponse.put("message", "Cliente actualizado correctamente");
            }

        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Error al actualizar cliente: " + e.getMessage());
            jsonResponse.put("error", e.getClass().getSimpleName());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        PrintWriter out = response.getWriter();
        out.print(jsonResponse.toString());
        out.flush();
    }

    // DELETE - Eliminar cliente
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        configurarCORS(response);
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        EntityManager em = null;
        JSONObject jsonResponse = new JSONObject();

        try {
            String codiParam = request.getParameter("codiClie");

            if (codiParam == null || codiParam.trim().isEmpty()) {
                throw new Exception("Parámetro codiClie es requerido");
            }

            int codiClie;
            try {
                codiClie = Integer.parseInt(codiParam);
            } catch (NumberFormatException e) {
                throw new Exception("El parámetro codiClie debe ser un número válido");
            }

            em = getEntityManager();
            em.getTransaction().begin();
            Cliente cliente = em.find(Cliente.class, codiClie);

            if (cliente == null) {
                em.getTransaction().rollback();
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Cliente no encontrado con ID: " + codiClie);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } else {
                em.remove(cliente);
                em.getTransaction().commit();

                jsonResponse.put("success", true);
                jsonResponse.put("message", "Cliente eliminado correctamente");
                jsonResponse.put("deletedId", codiClie);
            }

        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Error al eliminar cliente: " + e.getMessage());
            jsonResponse.put("error", e.getClass().getSimpleName());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        PrintWriter out = response.getWriter();
        out.print(jsonResponse.toString());
        out.flush();
    }
}
