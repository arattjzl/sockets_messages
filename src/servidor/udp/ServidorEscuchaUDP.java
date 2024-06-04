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

public class ServidorEscuchaUDP extends Thread{
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    protected DatagramSocket socket;
    protected final int PUERTO_SERVER;
    protected final JFrame frame;

    public ServidorEscuchaUDP(int puertoS) throws Exception{

        //Creamos el socket
        PUERTO_SERVER=puertoS;
        socket = new DatagramSocket(puertoS);
        frame = new JFrame("Servidor Video");
    }

    public void run() {
        try {
            EntradaSalida.mostrarMensaje("Servidor esperando videollamada.");
            recibeVideollamada();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private void procesaMensaje(Mensaje mensaje) throws Exception{
        String mensajeComp ="";

        if (mensaje.getMensaje().startsWith("fin")) {
            mensajeComp="Transmisión con el servidor finalizada...";
            mensaje.setMensaje(mensajeComp);
            enviaMensaje(mensaje);
        }
        else if (mensaje.getMensaje().startsWith("hola")) {
            mensajeComp="¿Cómo estas?";

            //formateamos el mensaje de salida
            mensaje.setMensaje(mensajeComp);
            enviaMensaje(mensaje);
        }
        else if (mensaje.getMensaje().startsWith("bien y tú")) {
            mensajeComp="También estoy bien, gracias";

            //formateamos el mensaje de salida
            mensaje.setMensaje(mensajeComp);
            enviaMensaje(mensaje);
        }
        else{
            mensajeComp="...";
        }
    }

    private Mensaje recibeMensaje() throws Exception{
        Mensaje mensajeObj=new Mensaje();
        String mensaje="";
        byte[] mensaje_bytes;
        byte[] mensaje2_bytes;
        final int MAX_BUFFER=256;
        DatagramPacket paquete;

        // Recibimos el paquete
        mensaje_bytes=new byte[MAX_BUFFER];
        paquete = new DatagramPacket(mensaje_bytes,MAX_BUFFER);
        socket.receive(paquete);

        // Lo formateamos
        //mensaje_bytes=new byte[paquete.getLength()];
        mensaje_bytes=paquete.getData();
        mensaje = new String(mensaje_bytes,0,paquete.getLength()).trim();
        mensajeObj.setMensaje(mensaje);

        //Obtenemos IP Y PUERTO
        mensajeObj.setPuertoCliente(paquete.getPort());
        mensajeObj.setAddressCliente(paquete.getAddress());

        // Lo mostramos por pantalla
        EntradaSalida.mostrarMensaje("Mensaje recibido \""+mensajeObj.getMensaje()+"\" del cliente "+
                mensajeObj.getAddressCliente()+":"+mensajeObj.getPuertoCliente()+"\n");

        return mensajeObj;
    }
    private void enviaMensaje(Mensaje mensajeObj) throws Exception{
        byte[] mensaje2_bytes = new byte[mensajeObj.getMensaje().length()];
        DatagramPacket envPaquete;

        mensaje2_bytes = mensajeObj.getMensaje().getBytes();

        //Preparamos el paquete que queremos enviar
        envPaquete = new DatagramPacket(mensaje2_bytes,mensaje2_bytes.length,mensajeObj.getAddressCliente(),mensajeObj.getPuertoCliente());

        // realizamos el envio
        socket.send(envPaquete);

        EntradaSalida.mostrarMensaje("Mensaje saliente del servidor \""+
                mensajeObj.getMensaje()+"\" al cliente " + mensajeObj.getAddressCliente() + ":"+mensajeObj.getPuertoCliente()+"\n");
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
    }

    private void recibeVideollamada() throws Exception{
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640, 480);
        frame.setVisible(true);

        byte[] buffer = new byte[65535];

        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());
            Mat mat = Imgcodecs.imdecode(new MatOfByte(packet.getData()), Imgcodecs.IMREAD_UNCHANGED);

            if (mat != null && !mat.empty()) {
                BufferedImage image = matToBufferedImage(mat);
                displayImage(image);
            }
        }
    }
}
