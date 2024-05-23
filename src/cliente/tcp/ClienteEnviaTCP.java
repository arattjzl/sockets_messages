package cliente.tcp;

import datos.EntradaSalida;
import datos.Mensaje;

import java.io.*;
import java.net.*;

public class ClienteEnviaTCP extends Thread {
    protected Socket socket;
    protected final int PUERTO_SERVER;
    protected final String SERVER;

    public ClienteEnviaTCP(String servidor, int puertoS) throws Exception {
        PUERTO_SERVER = puertoS;
        SERVER = servidor;
        socket = new Socket(SERVER, PUERTO_SERVER);
    }

    public void run() {
        try {
            Mensaje mensajeObj = new Mensaje();
            EntradaSalida.mostrarMensaje("Cliente conectado con servidor " +
                    socket.getInetAddress() + ":" + socket.getPort() + "...\n");
            EntradaSalida.mostrarMensaje("Cliente listo para mandar...\n");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            do {
                String mensaje = in.readLine();
                mensajeObj.setMensaje(mensaje);

                if (mensaje.startsWith("file:")) {
                    String filePath = mensaje.substring(5).trim();
                    enviaArchivo(filePath, out);
                } else {
                    out.writeUTF(mensaje);
                }

                EntradaSalida.mostrarMensaje("Mensaje \"" + mensajeObj.getMensaje() +
                        "\" enviado a " + mensajeObj.getAddressServidor() + ":" + mensajeObj.getPuertoServidor() + "\n");
            } while (!mensajeObj.getMensaje().startsWith("fin"));

        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private void enviaArchivo(String filePath, DataOutputStream out) throws Exception {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException("El archivo no existe o no es un archivo válido.");
        }

        FileInputStream fileIn = new FileInputStream(file);
        BufferedInputStream bufIn = new BufferedInputStream(fileIn);

        out.writeUTF("file:" + file.getName());
        out.writeLong(file.length());

        byte[] buffer = new byte[4096];
        int bytesRead;

        int totalBytesRead = 0;
        double tiempoTranscurrido = 0;
        double tiempoInicio = System.currentTimeMillis();
        double tasaTransferencia = 0;
        while ((bytesRead = bufIn.read(buffer)) != -1) {
            tiempoTranscurrido = (System.currentTimeMillis() - tiempoInicio) / 1000.0;
            tasaTransferencia = totalBytesRead / tiempoTranscurrido;
            String tiempoTranscurridoStr = tiempo(tiempoTranscurrido);
            String tasaTransferenciaStr = tasaTransferencia(tasaTransferencia);
            EntradaSalida.mostrarMensaje("Tasa de transferencia" + tasaTransferenciaStr + "s\n"
                                        + "Tiempo transcurrido" +  tiempoTranscurridoStr + "\n"
            );
            out.write(buffer, 0, bytesRead);
        }

        bufIn.close();
        EntradaSalida.mostrarMensaje("Archivo \"" + file.getName() + "\" enviado.\n");
    }

    private String tiempo(double tiempo) {
        int minutos = (int) (tiempo / 60);
        int segundos = (int) (tiempo % 60);
        return String.format("%02d:%02d", minutos, segundos);
    }

    private String tasaTransferencia(double tasa) {
        String[] unidades = {"B/s", "KB/s", "MB/s", "GB/s", "TB/s"};
        int index = 0;
        while (tasa >= 1024 && index < unidades.length - 1) {
            tasa /= 1024;
            index++;
        }
        return String.format("%.2f %s", tasa, unidades[index]);
    }
}
