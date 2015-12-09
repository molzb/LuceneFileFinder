package de.bernhard;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.lucene.queryparser.classic.ParseException;

@WebServlet(name = "FileManagerServlet", urlPatterns = {"/FileManagerServlet"})
public class FileManagerServlet extends HttpServlet {

	private static final Logger LOG = Logger.getLogger(FileManagerServlet.class.getName());

	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			FileManagerLucene fm = new FileManagerLucene();
			LOG.info("term=" + request.getParameter("term"));
			List<FileModel> res = fm.searchTerm(request.getParameter("term"));
			out.println(fm.toJson(res));
		} catch (ParseException ex) {
			LOG.log(Level.SEVERE, null, ex);
		} finally {
			out.close();
		}
	}

	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	@Override
	public String getServletInfo() {
		return "Short description";
	}// </editor-fold>
}