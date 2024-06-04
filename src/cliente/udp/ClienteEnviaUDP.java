package cliente.udp;

import datos.EntradaSalida;
import datos.Mensaje;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;
 
//declaramos la clase udp envia
public class ClienteEnviaUDP extends Thread{
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    //Definimos el sockets.
    protected final int PUERTO_SERVER;
    protected final String SERVER;
    protected DatagramSocket socket;

    public ClienteEnviaUDP(DatagramSocket nuevoSocket, String servidor, int puertoServidor){
        socket = nuevoSocket;
        SERVER=servidor;
        PUERTO_SERVER=puertoServidor;
    }
    
    public void run() {
        try {
            enviaVideollamada();
        }
        catch (Exception e) {
            System.err.println("Exception "+e.getMessage());
            System.exit(1);
        }

    }

    private void enviaMensaje(Mensaje mensajeObj) throws Exception{
        BufferedReader in= new BufferedReader(new InputStreamReader(System.in));
        byte[] mensaje_bytes;
        String mensaje="";
        DatagramPacket paquete;

        InetAddress addressServer=InetAddress.getByName(SERVER);
        mensaje = in.readLine();
        //mensaje_bytes=new byte[mensaje.length()];
        mensaje_bytes = mensaje.getBytes();
        paquete = new DatagramPacket(mensaje_bytes,mensaje.length(),addressServer,PUERTO_SERVER);
        socket.send(paquete);

        String mensajeMandado=new String(paquete.getData(),0,paquete.getLength()).trim();
        mensajeObj.setMensaje(mensajeMandado);
        mensajeObj.setAddressServidor(paquete.getAddress());
        mensajeObj.setPuertoServidor(paquete.getPort());

        EntradaSalida.mostrarMensaje("Mensaje \""+ mensajeObj.getMensaje() +
                "\" enviado a "+mensajeObj.getAddressServidor() + ":"+mensajeObj.getPuertoServidor()+"\n");
    }

    private void enviaVideollamada() throws Exception{
        VideoCapture capture = new VideoCapture(0);

        if (!capture.isOpened()) {
            System.err.println("Error al abrir la cámara");
            return;
        }

        InetAddress addressServer = InetAddress.getByName(SERVER);
        Mat frame = new Mat();

        while (true) {
            capture.read(frame);

            if (!frame.empty()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] imageBytes = new byte[(int) (frame.total() * frame.elemSize())];
                frame.get(0, 0, imageBytes);

                DatagramPacket packet = new DatagramPacket(imageBytes, imageBytes.length, addressServer, PUERTO_SERVER);
                socket.send(packet);

                Thread.sleep(100); // 10 FPS
            }
        }
    }
}
