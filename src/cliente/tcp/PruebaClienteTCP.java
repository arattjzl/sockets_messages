package cliente.tcp;

public class PruebaClienteTCP{
    public static void main(String args[])throws Exception{
        ClienteTCP clienteTCP =new ClienteTCP("164.220.10.49",60000);
             
        clienteTCP.inicia();
    }
}
