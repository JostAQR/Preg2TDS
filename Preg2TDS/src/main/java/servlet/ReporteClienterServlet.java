/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

/**
 *
 * @author Quichiz
 */
@WebServlet(name = "ReporteClienterServlet", urlPatterns = {"/ReporteClienterServlet"})
public class ReporteClienterServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Ruta del archivo .jasper compilado
            String pathJasper = getServletContext().getRealPath("/reporte/reportClientes.jasper");

            // Parámetros si los necesitas
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("isBlankWhenNull", true); // si lo usas como parámetro

            // Conexión a la base de datos
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/clintes", "root", "");

            // Generar el reporte
            JasperPrint jasperPrint = JasperFillManager.fillReport(pathJasper, parametros, conn);

            response.setContentType("application/pdf");
            JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

}
