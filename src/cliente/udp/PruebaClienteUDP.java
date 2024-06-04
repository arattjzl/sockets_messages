package cliente.udp;

public class PruebaClienteUDP{
    public static void main(String args[]) throws Exception{
        ClienteUDP clienteUDP =new ClienteUDP("192.168.1.88",50000);
        
        clienteUDP.inicia();
    }
}
