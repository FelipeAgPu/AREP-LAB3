package eci;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class HttpServer {
    private static final MimeTypeGenerator generator = new MimeTypeGenerator();
    public static void main(String[] args) throws IOException, URISyntaxException {
        //int port = Integer.parseInt(System.getenv("PORT"));
        int port = 5000;

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.printf("Could not listen on port: %s.%n",port);
            System.exit(1);
        }
        Boolean running = Boolean.TRUE;
        Socket clientSocket = null;
        while (running) {
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            OutputStream outputStream = clientSocket.getOutputStream();
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine, outputLine;
            URI path = new URI("");

            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received: " + inputLine);

                if (!in.ready()) {
                    break;
                }
                try {
                    path = new URI(inputLine.split(" ")[1]);
                }catch (Exception e){
                    System.out.println(e);
                }
                break;
            }

            try {
                outputLine = getHeaders(200, getType(path)) + getFile(path.getPath(), outputStream);
            }catch (IOException e){
                outputLine = getHeaders(404, "text/html") + getFile("404.html", outputStream);
            }

            out.println(outputLine);

            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }

    private static String getType(URI uri) {
        String type;
        try {
            type = uri.getPath().split("\\.")[1];
        } catch (Exception e) {
            return generator.getType("html");
        }

        return generator.getType(type);
    }

    private static  String getHeaders(int code, String type){
        return String.format("HTTP/1.1 %s OK \r\n"
                + "Content-type: %s \r\n"
                + "\r\n", code, type);
    }

    public static String getFile(String route, OutputStream outputStream) throws IOException {
        Path path = FileSystems.getDefault().getPath("src/main/resources", route);
        try {
            switch (route.split("\\.")[1]) {
                case "png":
                    return getImage(path, outputStream);
                default:
                    return getText(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e){
            return getText(FileSystems.getDefault().getPath("src/main/resources", "404.html"));
        }
        return null;
    }

    public static String getText(Path path) throws IOException {
        Charset charset = StandardCharsets.US_ASCII;
        StringBuilder file = new StringBuilder();
        BufferedReader reader = Files.newBufferedReader(path, charset);
        String line = null;
        while ((line = reader.readLine()) != null) {
            file.append(line).append("\n");
        }

        return file.toString();
    }

    static String getImage(Path file, OutputStream outputStream) throws IOException{
        String response = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: image/png\r\n"
                + "\r\n";
        BufferedImage bufferedImage = ImageIO.read(new File(file.toUri()));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
        dataOutputStream.writeBytes(response);
        dataOutputStream.write(byteArrayOutputStream.toByteArray());

        return response;
    }
}

