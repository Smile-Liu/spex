package servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.spex.web.util.UrlPathHelper;

public class DefaultServlet extends HttpServlet {

	private static final long serialVersionUID = 4520251858443560942L;

	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		UrlPathHelper helper = new UrlPathHelper();
		System.out.println(helper.getLookupPathForRequest(request));
	}
}
