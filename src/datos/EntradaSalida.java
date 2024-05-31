package datos;

import java.util.Scanner;

public class EntradaSalida {
    public static void mostrarMensaje(String mensaje){
        System.out.print(mensaje);
    }
    public static String ingresarTexto(){
        Scanner teclado = new Scanner(System.in);
        return teclado.nextLine();
    }
}
