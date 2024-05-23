package cliente.tcp;

public class PruebaClienteTCP{
    public static void main(String args[])throws Exception{
        ClienteTCP clienteTCP =new ClienteTCP("192.168.1.69filef",60000);
             
        clienteTCP.inicia();
    }
}
