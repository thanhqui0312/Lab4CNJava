import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.*;


@WebServlet(urlPatterns = {"/ProductService"})
public class ProductServlet extends HttpServlet {
    List<Product> products = new ArrayList<>();
    @Override
    public void init() throws ServletException {
        products.add(new Product(1,"Iphone 10", 5000));
        products.add(new Product(2,"Iphone 11", 7000));
        products.add(new Product(3,"Iphone X", 2000));
        products.add(new Product(3,"Iphone 13", 8000));
        products.add(new Product(3,"Iphone 14", 8500));
        products.add(new Product(3,"Iphone 15", 10000));
    }

    private JsonArray getAllProducts() {
        JsonArray jsonProducts = new JsonArray();
        for (Product product : products) {
            JsonObject jsonProduct = new JsonObject();
            jsonProduct.addProperty("id", product.getId());
            jsonProduct.addProperty("name", product.getName());
            jsonProduct.addProperty("price", product.getPrice());
            jsonProducts.add(jsonProduct);
        }
        return jsonProducts;
    }

    private Product getProductById(int id) {
        for (Product product : products) {
            if (product.getId() == id) {
                return product;
            }
        }
        return null;
    }


    private void sendSuccessResponse(HttpServletResponse response, String message, Object data) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("id", 0);
        jsonResponse.addProperty("message", message);
        if (data != null) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonElement dataJson = gson.toJsonTree(data);
            jsonResponse.add("data", dataJson);
        }
        out.print(jsonResponse.toString());
        out.flush();
    }

    private void sendErrorResponse(HttpServletResponse response, int errorCode, String errorMessage, Object errorData) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("id", errorCode);
        jsonResponse.addProperty("message", errorMessage);
        if (errorData != null) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonElement errorDataJson = gson.toJsonTree(errorData);
            jsonResponse.add("data", errorDataJson);
        }
        out.print(jsonResponse.toString());
        out.flush();
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int id = 0;

        if (req.getParameter("id") != null) {
            try {
                id = Integer.parseInt(req.getParameter("id"));
            } catch (NumberFormatException e) {
                sendErrorResponse(resp, 1, "Đối số không hợp lệ!", e);
                return;
            }

            Product product = getProductById(id);
            if (product != null) {
                sendSuccessResponse(resp, "Đọc sản phẩm thành công",product);
            } else {
                sendErrorResponse(resp, 2, "Không tìm thấy sản phẩm có id = " + id, null);
            }
        } else {
            sendSuccessResponse(resp, "Đọc sản phẩm thành công",getAllProducts());
        }
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject result = new JsonObject();

        String id = request.getParameter("id");
        String name = request.getParameter("name");
        String price = request.getParameter("price");


        if (id == null || id.isEmpty() || !id.matches("\\d+")) {
            sendErrorResponse(response,400,"Invalid input: id phải là 1 số",null);
            return;
        }

        if (name == null || name.isEmpty()) {
            sendErrorResponse(response,400,"Invalid input: cần có name",null);
            return;
        }

        if (price == null || price.isEmpty() || !price.matches("\\d+")) {
            sendErrorResponse(response,400,"Invalid input: price phải là 1 số",null);
            return;
        }
        int pid = Integer.parseInt(request.getParameter("id"));
        if (getProductById(pid) != null) {
            sendErrorResponse(response,400,id + " đã tồn tại",null);
            return;
        }

        Product newProduct = new Product(pid, name, Integer.parseInt(price));
        products.add(newProduct);

        sendSuccessResponse(response,"Success",newProduct);
    }


    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            String id = request.getParameter("id");
            String name = request.getParameter("name");
            String price = request.getParameter("price");


            if (id == null || id.isEmpty() || name == null || name.isEmpty() || price == null || price.isEmpty()) {
                sendErrorResponse(response, 1, "Thiếu dữ liệu!",null);
                return;
            }

            int pid = Integer.parseInt(id);

            Product productToUpdate = getProductById(pid);
            if (productToUpdate == null) {
                sendErrorResponse(response, 2, "Không tìm thấy sản phẩm",null);
                return;
            }

            productToUpdate.setName(name);
            productToUpdate.setPrice(Integer.parseInt(price));

            sendSuccessResponse(response, "Cập nhật thành công",productToUpdate);

        } catch (JsonSyntaxException e) {
            sendErrorResponse(response, 3, "Yêu cần không hợp lệ", e);
            e.printStackTrace();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));

            Product productToDelete = getProductById(id);
            if (productToDelete == null) {
                sendErrorResponse(response, 4, "Không tìm thấy sản phẩm",null);
                return;
            }
            products.remove(productToDelete);
            sendSuccessResponse(response, "Xóa sản phẩm thành công", null);

        } catch (NumberFormatException e) {
            sendErrorResponse(response, 5, "Đối số không hợp lệ",e);
            e.printStackTrace();
        }
    }

}
