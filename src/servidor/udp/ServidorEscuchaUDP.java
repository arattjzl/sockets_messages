package servidor.udp;

import datos.EntradaSalida;
import datos.Mensaje;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.net.*;
import java.io.*;

import org.opencv.core.Core;
import org.opencv.core.MatOfByte;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import javax.swing.*;

public class ServidorEscuchaUDP extends Thread {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    protected DatagramSocket socket;
    protected final int PUERTO_SERVER;
    protected final JFrame frame;
    protected JLabel label;

    public ServidorEscuchaUDP(int puertoS) throws Exception {
        PUERTO_SERVER = puertoS;
        socket = new DatagramSocket(puertoS);
        frame = new JFrame("Servidor Video");
        label = new JLabel();
    }

    public void run() {
        try {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(640, 480);
            frame.add(label);
            frame.setVisible(true);

            EntradaSalida.mostrarMensaje("Servidor esperando videollamada.");
            recibeVideollamada();
        } catch (Exception e) {
            System.err.println("Error en el servidor: " + e.getMessage());
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    private void procesaMensaje(Mensaje mensaje) throws Exception {
        String mensajeComp = "";

        if (mensaje.getMensaje().startsWith("fin")) {
            mensajeComp = "Transmisión con el servidor finalizada...";
            mensaje.setMensaje(mensajeComp);
            enviaMensaje(mensaje);
        } else if (mensaje.getMensaje().startsWith("hola")) {
            mensajeComp = "¿Cómo estas?";
            mensaje.setMensaje(mensajeComp);
            enviaMensaje(mensaje);
        } else if (mensaje.getMensaje().startsWith("bien y tú")) {
            mensajeComp = "También estoy bien, gracias";
            mensaje.setMensaje(mensajeComp);
            enviaMensaje(mensaje);
        } else {
            mensajeComp = "...";
        }
    }

    private Mensaje recibeMensaje() throws Exception {
        Mensaje mensajeObj = new Mensaje();
        byte[] mensaje_bytes = new byte[256];
        DatagramPacket paquete = new DatagramPacket(mensaje_bytes, mensaje_bytes.length);
        socket.receive(paquete);

        String mensaje = new String(paquete.getData(), 0, paquete.getLength()).trim();
        mensajeObj.setMensaje(mensaje);
        mensajeObj.setPuertoCliente(paquete.getPort());
        mensajeObj.setAddressCliente(paquete.getAddress());

        EntradaSalida.mostrarMensaje("Mensaje recibido \"" + mensajeObj.getMensaje() + "\" del cliente " +
                mensajeObj.getAddressCliente() + ":" + mensajeObj.getPuertoCliente() + "\n");

        return mensajeObj;
    }

    private void enviaMensaje(Mensaje mensajeObj) throws Exception {
        byte[] mensaje2_bytes = mensajeObj.getMensaje().getBytes();
        DatagramPacket envPaquete = new DatagramPacket(mensaje2_bytes, mensaje2_bytes.length, mensajeObj.getAddressCliente(), mensajeObj.getPuertoCliente());
        socket.send(envPaquete);

        EntradaSalida.mostrarMensaje("Mensaje saliente del servidor \"" + mensajeObj.getMensaje() +
                "\" al cliente " + mensajeObj.getAddressCliente() + ":" + mensajeObj.getPuertoCliente() + "\n");
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] buffer = new byte[bufferSize];
        mat.get(0, 0, buffer);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }

    private void displayImage(Image img) {
        ImageIcon icon = new ImageIcon(img);
        JLabel label = new JLabel(icon);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(label);
        frame.revalidate();
        frame.repaint();
    }


    private void recibeVideollamada() throws Exception {
        byte[] buffer = new byte[65535];

        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            Mat mat = Imgcodecs.imdecode(new MatOfByte(packet.getData()), Imgcodecs.IMREAD_UNCHANGED);

            if (mat != null && !mat.empty()) {
                BufferedImage image = matToBufferedImage(mat);
                displayImage(image);
            } else {
                System.err.println("Frame no valido");
            }
        }
    }
}
