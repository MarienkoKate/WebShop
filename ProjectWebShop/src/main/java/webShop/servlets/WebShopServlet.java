package webShop.servlets;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import webShop.exception.WebShopException;
import webShop.io.ReadProducts;
import webShop.io.WriteProducts;
import webShop.model.Product;
import webShop.model.ShoppingCart;

@WebServlet("/webShop")
public class WebShopServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
   
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		
		String sortRequest = request.getParameter("sort");
		String categoryRequest = request.getParameter("category");
		
		String sortSession = (String) session.getAttribute("sort");
		String categorySession = (String) session.getAttribute("category");
		
		if(sortRequest != null) {
			sortSession = sortRequest;
			session.setAttribute("sort", sortRequest);
		}else if(sortSession == null) {
			sortSession = "cost";
			session.setAttribute("sort", sortSession);
		}
		if(categoryRequest != null) {
			categorySession =  categoryRequest;
			session.setAttribute("category", categoryRequest);
		}else if(categorySession == null) {
			categorySession = "ALL";
			session.setAttribute("category", categorySession);
		}
		
        String password = (String) session.getAttribute("password"); 
        if(password == null) {
        	mainPage(request, response, sortSession, categorySession, false);
        }else {
        	mainPage(request, response, sortSession, categorySession, true);
        }
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sort, id;
        sort = request.getParameter("sort");
		id = request.getParameter("id");
		ArrayList<Product> products = uploadListFromBase();
		if(products == null) {
			doGet(request, response);
		}
		switch(sort) {
		case "alph":
			Collections.sort(products, Product.alphComparator);
			break;
		case "cost":
			Collections.sort(products, Product.costComparator);
			break;
		case "date":
			Collections.sort(products, Product.dateComparator);
			break;
		}
		HttpSession session = request.getSession();
		ShoppingCart cart = (ShoppingCart)session.getAttribute("cart");
		if(cart == null) {
			cart = new ShoppingCart();
			session.setAttribute("cart", cart);
		}
		Product product = products.get(Integer.parseInt(id));
		if(product.getNumberOfProduct() > 0) {
			product.setNumberOfProduct(product.getNumberOfProduct() - 1);
			cart.addToCart(products.get(Integer.parseInt(id)));
			saveToBase(products);
			doGet(request, response);
		}else {
			messageErrorNotAvailableProduct(request, response);
		}
	}

	private void saveToBase(ArrayList<Product> products) {
		String basePath = "base/base.txt";
		ServletContext context = getServletContext();
		basePath = context.getRealPath(basePath); 
		try {
			WriteProducts wp = new WriteProducts(basePath);
			wp.writeProducts(products);
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void messageErrorNotAvailableProduct(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter pw = response.getWriter();
		String pathCSS1 = "static/style.css";
		String pathCSS2 = "static/menu.css";
		
		pw.println("<!DOCTYPE html> <html> ");
        pw.println("<head>");
        pw.println("<title>Edit product</title>");
		pw.println("<link rel=\"stylesheet\" href=\"" + pathCSS1 + "\"type=\"text/css\">");
		pw.println("<link rel=\"stylesheet\" href=\"" + pathCSS2 + "\"type=\"text/css\">");
        pw.println("</head>");
		pw.println("<body>");
		pw.println("<h1 class = 'heading'>Add to cart</h1>");
		pw.println("<h2 class = 'littleHeading'>Sorry, you can't add this product to cart :(</h1>");
		pw.println("<a href= 'webShop' class = 'button' >main<br>page</a>");
		pw.println("</body> </html>");
	}
	
	private void mainPage(HttpServletRequest request, HttpServletResponse response, String sort, String category, boolean admin) throws IOException {
		PrintWriter pw = response.getWriter();
		String pathCSS1 = "static/style.css";
		String pathCSS2 = "static/menu.css";
		
		pw.println("<!DOCTYPE html> <html> ");
        pw.println("<head>");
        pw.println("<title>Web Shop</title>");
		pw.println("<link rel=\"stylesheet\" href=\"" + pathCSS1 + "\"type=\"text/css\">");
		pw.println("<link rel=\"stylesheet\" href=\"" + pathCSS2 + "\"type=\"text/css\">");
        pw.println("</head>");
		
        pw.println("<body>");
        pw.println("<h1 class = 'heading'>Web Shop</h1>");
        //âûâîä ìåíþøêè ñ êàòåãîðèÿìè
        pw.println(createMenuCategories(admin));
        switch(sort) {
	        case "cost":
	        	pw.println(listOfProductSortedByCost(admin, category));
	        	break;
	        case "alph":
	        	pw.println(listOfProductSortedByAlph(admin, category));
	        	break;
	        case "date":
	        	pw.println(listOfProductSortedByDate(admin, category));
	        	break;
        }
        pw.println("</body>");
        pw.println("</html>");
	}
	
	private ArrayList<Product> uploadListFromBase() {
		ArrayList<Product> products = null;
		String text = "it ok?";
		String basePath = "base/base.txt";
		ServletContext context = getServletContext();
		basePath = context.getRealPath(basePath); 
		
			try {
			
			ReadProducts readProducts = new ReadProducts(basePath);
			products = readProducts.readFile();
			}catch(WebShopException wse) {
				wse.printStackTrace();
				
				return null;
			}catch(FileNotFoundException fnfe) {
				fnfe.printStackTrace();
				
				return null;
			}catch(Exception ex) {
				ex.printStackTrace();
				
				return null;
			}
			
		
		return products;
	}
	
	private String listOfProductSortedByDate(boolean admin, String category) {
		StringBuilder html = new StringBuilder();
		ArrayList<Product> products = uploadListFromBase();
		if(products == null) {
			return html.append("ERROR IN BASE").toString();
		}
		Collections.sort(products, Product.dateComparator);
		if(admin) {
			html.append(showAllProductForAdmin(products, category, "date"));
		}else {
			html.append(showAllProductsForUsers(products, category, "date"));
		}
		return html.toString();
	}
	
	private String listOfProductSortedByAlph(boolean admin, String category) {
		StringBuilder html = new StringBuilder();
		ArrayList<Product> products = uploadListFromBase();
		if(products == null) {
			return html.append("ERROR IN BASE").toString();
		}
		Collections.sort(products, Product.alphComparator);
		if(admin) {
			html.append(showAllProductForAdmin(products, category, "alph"));
		}else {
			html.append(showAllProductsForUsers(products, category, "alph"));
		}
		return html.toString();
	}
	
	private String listOfProductSortedByCost(boolean admin, String category) {
		StringBuilder html = new StringBuilder();
		ArrayList<Product> products = uploadListFromBase();
		if(products == null) {
			return html.append("ERROR IN BASE").toString();
		}
		Collections.sort(products, Product.costComparator);
		if(admin) {
			html.append(showAllProductForAdmin(products, category, "cost"));
		}else {
			html.append(showAllProductsForUsers(products, category, "cost"));
		}
		return html.toString();
	}
	
	private String showAllProductsForUsers(ArrayList<Product> products, String category, String sort) {
		StringBuilder html = new StringBuilder();
		html.append("<div id='allproducts'>");
		for(int i = 0; i < products.size(); i++) {
			Product p = products.get(i);
			if((!p.getCategories().equalsIgnoreCase(category) && !category.equals("ALL")) || p.getNumberOfProduct() == 0) {
				continue;
			}
			html.append("<div id ='sorting' style='display:none;'>cost</div>");
			
			html.append("<div class = 'productBox' id='prod" + i + "'>");  //ó êàæäîãî id prodX
			html.append("<table width='100%' cellspacing='0' cellpadding='0'>");
			html.append("<tr>");
			html.append("<td class='leftcol'><img src='image/" + p.getName() + ".jpg'");
			html.append("width='90' height='78' alt=''></td>");
			html.append("<td valign='top'>");
			html.append("<strong class = 'fontForText' align='left'>").append(p.getName()).append(" </strong><br>");
			html.append("<strong class = 'fontForText' align='left'>COST:").append(p.getCost()).append(" </strong><br>");
			html.append("<div class = 'fontForText' >AVAILABLE:").append(p.getNumberOfProduct()).append(" </div><br>");
			html.append("<i class = 'fontForText' align='left'>").append(p.getDateAdded()).append("</i>");
			html.append("<br>___________________________________________</br>");
			html.append("<br><div class = 'productDescription'>").append(p.getDescription()).append("</div>");
			//êíîïêà äîáàâèòü â êîðçèíêó
			html.append("<form action='webShop' method='post'>");
			html.append("<input type='hidden' name='sort' value='" + sort + "'>");
			html.append("<input type='hidden' name='user' value='user'>");
			html.append("<input type='hidden' name='id' value='" + i + "'>");
			html.append("<input type='image' src='image/addToCart.png' style='width:40px;height:40;' " + 
					" alt='Add to Cart' class = 'littleButton' />");
			html.append("</form>");
			html.append("</td>");		
			html.append("</tr>");
			html.append("</table>");
			html.append("</div>");
		}
		html.append("</div>");
		return html.toString();
	}
	
	private String showAllProductForAdmin(ArrayList<Product> products, String category, String sort) {
		StringBuilder html = new StringBuilder();
		//êíîïêà äîáàâëåíèÿ åùå òîâàðà
		html.append("<form action='addProduct' method='get'>");
		html.append("<input type='hidden' name='user' value='admin'>");
		html.append("</td></tr><tr><td>");
		html.append("<input type='image' src='image/add.png' style='width:40px;height:40;' " + 
				" alt='Edit product' class = 'littleButton' /><td>");
		html.append("</form>");
		
		html.append("<div id='allproducts'>");
		for(int i = 0; i < products.size(); i++) {
			Product p = products.get(i);
			if(!p.getCategories().equalsIgnoreCase(category) && !category.equals("ALL")) {
				continue;
			}
			html.append("<div id ='sorting' style='display:none;'>cost</div>");
			html.append("<div class = 'productBox' id='prod" + i + "'>");
			html.append("<table width='100%' cellspacing='0' cellpadding='0'>");
			html.append("<tr>");
			html.append("<td class='leftcol'><img src='image/" + p.getName() + ".jpg'");
			html.append("width='90' height='78' alt=''></td>");
			html.append("<td valign='top'>");
			html.append("<strong class = 'fontForText' align='left'>").append(p.getName()).append(" </strong><br>");
			html.append("<strong class = 'fontForText' align='left'>COST:").append(p.getCost()).append(" </strong><br>");
			html.append("<div class = 'fontForText' >AVAILABLE:").append(p.getNumberOfProduct()).append(" </div><br>");
			html.append("<i class = 'fontForText' align='left'>").append(p.getDateAdded()).append("</i>");
			html.append("<br>___________________________________________</br>");
			html.append("<br><div class = 'productDescription'>").append(p.getDescription()).append("</div>");
			//êíîïêà èçìåíåíèÿ
			html.append("<form action='editProduct' method='get'>");
			html.append("<input type='hidden' name='sort' value='" + sort + "'>");
			html.append("<input type='hidden' name='user' value='admin'>");
			html.append("<input type='hidden' name='id' value='" + i + "'>");
			html.append("</td></tr><tr><td>");
			html.append("<input type='image' src='image/edit.png' style='width:40px;height:40;' " + 
					" alt='Edit product' class = 'littleButton' /><td>");
			html.append("</form>");
			//êíîïêà óäàëåíèÿ
			html.append("<form action='deleteProduct' method='get'>");
			html.append("<input type='hidden' name='sort' value='" + sort + "'>");
			html.append("<input type='hidden' name='id' value='" + i + "'>");
			html.append("<input type='image' src='image/delete.png' style='width:40px;height:40;' " + 
					" alt='Delete product' class = 'littleButton' /><td>");
			html.append("</form>");
			html.append("</td>");		
			html.append("</tr>");
			html.append("</table>");
			html.append("</div>");
		}
		html.append("</div>");
		return html.toString();
	}
	
	private String createMenuCategories(boolean admin) {
		StringBuilder html = new StringBuilder();
		html.append("<nav>");
		html.append("<ul class='topmenu'>");
		if(!admin) {
			html.append("<li><a href='login'>Enter system</a></li>");
		}else {
			html.append("<li><a href='logout'>Logout</a></li>");
		}
		//îòêðûâàþùèé ñïèñîê ñ âèäàìè ñîðòèðîâîê
		html.append("<li><a href='' class='down'>Sorting by</a>");
		html.append("<ul class='submenu'>");
		//ïî öåíå
		html.append("<li>");
		html.append("<form action='webShop' method='get'>");
		html.append("<input type='submit' value='Cost'>");
		html.append("<input type='hidden' name='sort' value='cost'>");
		html.append("</form>");
		html.append("</li>");
		//ïî àëôàâèòó
		html.append("<li>");
		html.append("<form action='webShop' method='get'>");
		html.append("<input type='submit' value='Alphabet'>");
		html.append("<input type='hidden' name='sort' value='alph'>");
		html.append("</form>");
		html.append("</li>");
		//ïî äàòå
		html.append("<li>");
		html.append("<form action='webShop' method='get'>");
		html.append("<input type='submit' value='Date'>");
		html.append("<input type='hidden' name='sort' value='date'>");
		html.append("</form>");
		html.append("</li>");
		html.append("</ul>");
		html.append("</li>");
		
		//îòêðûâàþùèéñÿ ñïèñîê ñ êàòåãîðèÿìè
		html.append("<li><a href='' class='down'>Categories</a>");
		html.append("<ul class='submenu'>");
		//êàòåãîðèÿ -- âñå
		html.append("<li>");
		html.append("<form action='webShop' method='get'>");
		html.append("<input type='submit' value='ALL'>");
		html.append("<input type='hidden' name='category' value='ALL'>");
		html.append("</form>");
		html.append("</li>");
		//êàòåãîðèÿ òåëåôîíû
		html.append("<li>");
		html.append("<form action='webShop' method='get'>");
		html.append("<input type='submit' value='stuffed Toys'>");
		html.append("<input type='hidden' name='category' value='stuffed Toys'>");
		html.append("</form>");
		html.append("</li>");
		//êàòåãîðèÿ ÷àñû
		html.append("<li>");
		html.append("<form action='webShop' method='get'>");
		html.append("<input type='submit' value='constructor'>");
		html.append("<input type='hidden' name='category' value='constructor'>");
		html.append("</form>");
		html.append("</li>");
		//êàòåãîðèÿ íîóòáóêè
		html.append("<li>");
		html.append("<form action='webShop' method='get'>");
		html.append("<input type='submit' value='toys for toddlers'>");
		html.append("<input type='hidden' name='category' value='toys for toddlers'>");
		html.append("</form>");
		html.append("</li>");
		html.append("</ul>");
		html.append("</li>");
		html.append("<li><a href='cart'>Cart</a></li>");
		html.append("</ul>");
		html.append("</nav>");
		return html.toString();
	}
	
}
