package servidor.tcp;

import datos.EntradaSalida;
import datos.Mensaje;

import java.io.*;
import java.net.*;

public class ServidorEscuchaTCP extends Thread {
    protected ServerSocket socket;
    protected Socket socket_cli;
    protected final int PUERTO_SERVER;

    public ServidorEscuchaTCP(int puertoS) throws Exception {
        PUERTO_SERVER = puertoS;
        socket = new ServerSocket(PUERTO_SERVER);
    }

    public void run() {
        try {
            EntradaSalida.mostrarMensaje("Servidor escuchando...\n");
            socket_cli = socket.accept();
            EntradaSalida.mostrarMensaje("Servidor conectado con cliente " +
                    socket_cli.getInetAddress() + ":" + socket_cli.getPort() + "...\n");
            DataInputStream in = new DataInputStream(socket_cli.getInputStream());

            do {
                String mensaje = in.readUTF();
                Mensaje mensajeObj = new Mensaje();
                mensajeObj.setMensaje(mensaje);

                if (mensaje.startsWith("file:")) {
                    String fileName = mensaje.substring(5).trim();
                    long fileSize = in.readLong();
                    recibeArchivo(fileName, fileSize, in);
                } else {
                    mensajeObj.setAddressCliente(socket_cli.getInetAddress());
                    mensajeObj.setPuertoCliente(socket_cli.getPort());
                    EntradaSalida.mostrarMensaje("Mensaje recibido \"" + mensajeObj.getMensaje() + "\" de " +
                            mensajeObj.getAddressCliente() + ":" + mensajeObj.getPuertoCliente() + "\n");
                }
            } while (true);

        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private void recibeArchivo(String fileName, long fileSize, DataInputStream in) throws Exception {
        FileOutputStream fileOut = new FileOutputStream("recibido_" + fileName);
        BufferedOutputStream bufOut = new BufferedOutputStream(fileOut);
    
        byte[] buffer = new byte[4096];
        int bytesRead;
        long totalBytesRead = 0;
    
        while (totalBytesRead < fileSize && (bytesRead = in.read(buffer, 0, Math.min(buffer.length, (int)(fileSize - totalBytesRead)))) != -1) {
            bufOut.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;
        }
    
        bufOut.flush();
        bufOut.close();
        
        if (totalBytesRead == fileSize) {
            EntradaSalida.mostrarMensaje("Archivo \"" + fileName + "\" recibido correctamente.\n");
            mostrarContenidoArchivo("recibido_" + fileName); // Mostrar el contenido del archivo
        } else {
            EntradaSalida.mostrarMensaje("Error al recibir el archivo \"" + fileName + "\". Bytes leídos: " + totalBytesRead + " de " + fileSize + "\n");
        }
    }
    
    private void mostrarContenidoArchivo(String filePath) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            EntradaSalida.mostrarMensaje("Contenido del archivo:\n");
            while ((line = reader.readLine()) != null) {
                EntradaSalida.mostrarMensaje(line + "\n");
            }
            reader.close();
        } catch (IOException e) {
            EntradaSalida.mostrarMensaje("Error al leer el contenido del archivo.\n");
        }
    }
}
