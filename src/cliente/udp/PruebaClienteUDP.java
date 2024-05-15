package cliente.udp;

public class PruebaClienteUDP{
    public static void main(String args[]) throws Exception{
        ClienteUDP clienteUDP =new ClienteUDP("164.220.10.49",50000);
        
        clienteUDP.inicia();
    }
}
