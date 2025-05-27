package servlet;

import dao.ClienteJpaController;
import dto.Cliente;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
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
@WebServlet(name = "login", urlPatterns = {"/login"})
public class login extends HttpServlet {
    //LLAMAR AL CONTROLADOR - ACCESO A BD
    private ClienteJpaController loginService;

    //LLAMA A LA PERSISTENCIA
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_Preg2TDS_war_1.0-SNAPSHOTPU");

   @Override
    public void init() throws ServletException {
        super.init();
        //INICIALIZAR CONTROLADOR
        loginService = new ClienteJpaController(emf);
    }

    @Override
    // METODO QUE SE EJECUTA - SOLICITUD POST
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // INDICA LA RESPUESTA JSON - FORMATO PARA ESPAÑOL (Ñ,Á...)
        response.setContentType("application/json;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        // LEER EL CUERPO DEL REQUEST (SOLICITUD) - LA GUARDA EN SB
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        //CONVIERTE SB EN JSON
        JSONObject body = new JSONObject(sb.toString());

        //EXTRAE VALORES DE HTML EN BODY
        String usuario = body.getString("usuario");
        String clave = body.getString("clave");

        //VERIFICA LA EXISTENCIA DEL USUARIO
        Cliente user = loginService.validarCliente(usuario, clave);

        //OBJETO EN DONDE SE ENVIARA LA RESPUESTA
        JSONObject json = new JSONObject();
        
        //SI SE INGRESA UN USUARIO VALIDO
        if (user != null) {
            //AUTENTICA USUARIO
            request.getSession().setAttribute("usuario", user);
            //SE INDICA LA RESPUESTA
            json.put("status", "ok");
            //REDIRECCIONAR A PAGINA PRINCIPAL
            json.put("redirect", "principal.html");
        } else {
            //SE INDICA ERROR - FALLO
            json.put("status", "fail");
            json.put("message", "Usuario o clave incorrecta");
        }

        //SE ESCRIBE RESPUESTA
        out.print(json.toString());
        //ENVIA RESPUESTA AL NAVEGADOR
        out.flush();
    }
}
