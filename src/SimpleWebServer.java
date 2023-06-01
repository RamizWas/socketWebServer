import java.io.*;
import java.net.*;

public class SimpleWebServer {
    public static void main(String[] args) throws IOException {
        int port = 9977;
        ServerSocket serverSocket = new ServerSocket(port);
      
        System.out.println("Server listening on port " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

            HttpRequestHandler requestHandler = new HttpRequestHandler(clientSocket);
            requestHandler.start();
        }
    }
}

class HttpRequestHandler extends Thread {
    private Socket clientSocket;

    public HttpRequestHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            BufferedReader requestReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream responseStream = clientSocket.getOutputStream();

            String requestLine = requestReader.readLine();
            if (requestLine != null) {
                System.out.println("Received request: " + requestLine);

                String[] requestParts = requestLine.split(" ");
                String method = requestParts[0];
                String path = requestParts[1];

                if (method.equals("GET")) {
                    handleGetRequest(path, responseStream);
                } else {
                    sendErrorResponse(responseStream, 501, "Not Implemented");
                }
            }

            responseStream.close();
            requestReader.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleGetRequest(String path, OutputStream responseStream) throws IOException {

        File file = new File("htmlFile\\" + path);


        if (path.equals("/yt")) {
            sendRedirectResponse(responseStream, "https://www.youtube.com");
        } else if (path.equals("/so")) {
            sendRedirectResponse(responseStream, "https://stackoverflow.com");
        } else if (path.equals("/rt")) {
            sendRedirectResponse(responseStream, "https://ritaj.birzeit.edu/");
        } else if (path.equals("/ar")) {
            file = new File("htmlFile\\main_ar.html");
            path="main.html";

            String contentType1 = getContentType(file);
            byte[] fileData1 = readFile(file);

            sendOkResponse(responseStream, contentType1, fileData1);
        }else if (path.equals("/") || path.equals("/en") || path.equals("/main_en.html") || path.equals("/index.html")) {
            file = new File("htmlFile\\main_en.html");
            path="main.html";

            String contentType1 = getContentType(file);
            byte[] fileData1 = readFile(file);

            sendOkResponse(responseStream, contentType1, fileData1);
        }else if (file.exists()) {
            String contentType = getContentType(file);
            byte[] fileData = readFile(file);

            sendOkResponse(responseStream, contentType, fileData);
        } else {
            sendErrorResponse(responseStream, 404, "Not Found");
        }
    }

    private String getContentType(File file) {
        String fileName = file.getName();
        if (fileName.endsWith(".html")) {
            return "text/html";
        } else if (fileName.endsWith(".css")) {
            return "text/css";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".jpg")) {
            return "image/jpeg";
        } else {
            return "application/octet-stream";
        }
    }

    private byte[] readFile(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] fileData = fileInputStream.readAllBytes();
        fileInputStream.close();
        return fileData;
    }

    private void sendOkResponse(OutputStream responseStream, String contentType, byte[] responseData) throws IOException {
        responseStream.write(("HTTP/1.1 200 OK\r\n").getBytes());
        responseStream.write(("Content-Type: " + contentType + "\r\n").getBytes());
        responseStream.write(("Content-Length: " + responseData.length + "\r\n").getBytes());
        responseStream.write("\r\n".getBytes());
        responseStream.write(responseData);
    }

    private void sendRedirectResponse(OutputStream responseStream, String redirectUrl) throws IOException {
        responseStream.write(("HTTP/1.1 307 Temporary Redirect\r\n").getBytes());
        System.out.println("HTTP/1.1 307 Temporary Redirect\r\n");
        System.out.println("Location: " + redirectUrl + "\r\n");
        responseStream.write(("Location: " + redirectUrl + "\r\n").getBytes());
        responseStream.write("\r\n".getBytes());
    }

    private void sendErrorResponse(OutputStream responseStream, int statusCode, String statusText) throws IOException {
        String body = "<html><head><title>Error 404</title></head><body>" +
                "<h1>" + statusText + "</h1>" +

                "<p style='color: red'>The file is not found</p>" +
                "<p><strong>Your names and IDs:</strong> Ramiz Wasaya (1180903), Khalil Khawaja (1202472)</p>" +
                "<p><strong>Client IP and Port:</strong> " + clientSocket.getInetAddress().getHostAddress() + ":" +
                clientSocket.getPort() + "</p>" +
                "</body></html>";

        responseStream.write(("HTTP/1.1 " + statusCode + " " + statusText + "\r\n").getBytes());
        responseStream.write("Content-Type: text/html\r\n".getBytes());
        responseStream.write(("Content-Length: " + body.length() + "\r\n").getBytes());
        responseStream.write("\r\n".getBytes());
        responseStream.write(body.getBytes());
    }
}
