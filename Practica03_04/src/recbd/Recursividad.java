package recbd;
public class Recursividad {
	    public static void main(String[] args) throws ClassNotFoundException{
	    	Class.forName("org.sqlite.JDBC"); 
	    	
	       String frase1 = "Hola, mundo!";
	        String frase2 = "Estoy haciendo la practica de recursividad y bd";
	       
	        System.out.println("Frase original: " + frase1);
	        System.out.println("Frase invertida: " + invertirFrase(frase1));
	        System.out.println("\nFrase original: " + frase2);
	        System.out.println("Invertidas las palabras:"+invertirPalabras(frase2)); 
	       
	       
	    }
	   
	    //4.1
	    public static String invertirFrase(String frase) {
	        if (frase.isEmpty()) {
	            return frase;
	        } else {
	            return frase.charAt(frase.length() - 1) + invertirFrase(frase.substring(0, frase.length() - 1));
	       
	    }
	}
	    //4.2
	    public static String invertirPalabras(String texto) {
	        if (texto.isEmpty()) {
	            return texto;
	        }
	       
	        int indiceSeparador= encontrarIndiceSeparador(texto);
	       
	        if (indiceSeparador== -1) {
	            return invertirPalabras("") + texto;
	        } else {
	            String palabra= texto.substring(0, indiceSeparador);
	            String restoTexto= texto.substring(indiceSeparador + 1);
	            return invertirPalabras(restoTexto)+ " " +palabra;
	        }
	    }
	    public static int encontrarIndiceSeparador(String texto) {
	        for (int i = 0;i< texto.length(); i++) {
	            char caracter= texto.charAt(i);
	            if (caracter== ' ' || caracter == '\t' || caracter == '\n' || !Character.isLetterOrDigit(caracter)) {
	                return i;
	            }
	        }
	        return -1;
	    }
	
	
	   

	
}

