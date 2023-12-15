package recbd;

import java.sql.*;
import java.util.ArrayList;

public class BDCartas {
    private static String url = "jdbc:sqlite:GuardaManos.db";

    public static void main(String[] args) throws ClassNotFoundException {
        inicializarBaseDatos();

        ArrayList<Carta> filtroPoker=new ArrayList<>();
        filtroPoker.add(new Carta("Corazones", 1));
        filtroPoker.add(new Carta("Diamantes", 1));
        filtroPoker.add(new Carta("Treboles", 1));
        filtroPoker.add(new Carta("Picas", 1));

        ArrayList<Carta> filtroFull=new ArrayList<>();
        filtroFull.add(new Carta("Corazones", 2));
        filtroFull.add(new Carta("Diamantes", 2));
        filtroFull.add(new Carta("Treboles", 2));
        filtroFull.add(new Carta("Corazones", 3));
        filtroFull.add(new Carta("Diamantes", 3));

        System.out.println("Guardando Poker:");
        guardaManos(filtroPoker, "Poker");
        System.out.println("Guardando Full:");
        guardaManos(filtroFull, "Full");
    }

  public static void posiblesManos(int n, ArrayList<Carta> baraja, ArrayList<Carta> manoActual, int inEmp) {
      if (n== 0) {

          for (Carta carta:manoActual) {
              System.out.print(carta + " ");
          }
          System.out.println();
          return;
      }
      for (int i=inEmp; i<baraja.size(); i++) {
          manoActual.add(baraja.get(i));
          posiblesManos(n-1, baraja, manoActual, i+ 1);
          manoActual.remove(manoActual.size()- 1);
      }
  }
 
 
  public static void filtraManos(int n, ArrayList<Carta> baraja, ArrayList<Carta> manoActual, int intEmp) {
      if (n==0) {

          if (cumpleCondicion(manoActual)) {

              for (Carta carta: manoActual) {
                  System.out.print(carta + " ");
              }
              System.out.println();
          }
          return;
      }
      for (int i=intEmp; i< baraja.size(); i++) {

          manoActual.add(baraja.get(i));
          filtraManos(n-1, baraja, manoActual, i+ 1);

          manoActual.remove(manoActual.size()- 1);
      }
  }
  private static boolean cumpleCondicion(ArrayList<Carta> mano) {
     
      for (Carta carta: mano) {
          if (carta.getValor()== 1) {
              return true; 
          }
      }
      return false;
  }
 
  private static void inicializarBaseDatos() throws ClassNotFoundException {
      try {
          Class.forName("org.sqlite.JDBC");
          try (Connection conn = DriverManager.getConnection(url)) {
              Statement stmt = conn.createStatement();
              stmt.execute("CREATE TABLE IF NOT EXISTS Carta (id INTEGER PRIMARY KEY AUTOINCREMENT, palo TEXT, valor INTEGER)");
              stmt.execute("CREATE TABLE IF NOT EXISTS Filtro (id INTEGER PRIMARY KEY AUTOINCREMENT, codigo TEXT, textoExplicativo TEXT)");
              stmt.execute("CREATE TABLE IF NOT EXISTS ManoFiltro (id INTEGER PRIMARY KEY AUTOINCREMENT, idFiltro INTEGER, FOREIGN KEY (idFiltro) REFERENCES Filtro(id))");
              stmt.execute("CREATE TABLE IF NOT EXISTS CartaMano (id INTEGER PRIMARY KEY AUTOINCREMENT, idManoFiltro INTEGER, idCarta INTEGER, FOREIGN KEY (idManoFiltro) REFERENCES ManoFiltro(id), FOREIGN KEY (idCarta) REFERENCES Carta(id))");
        
          }
      } catch (SQLException e) {
          e.printStackTrace();
      }
  }
  public static void guardaManos(ArrayList<Carta> mano, String tipoFiltro) {
      try (Connection conn=DriverManager.getConnection(url)) {
          conn.setAutoCommit(false);


          int idFiltro=obtenerOInsertarFiltro(conn,tipoFiltro);


          int idManoFiltro=obtenerOInsertarManoFiltro(conn, idFiltro, mano);


          if (cumpleCondicion(mano, tipoFiltro)) {
              System.out.println("La mano cumple con la condición del filtro: "+ tipoFiltro);
              relacionarCartasConMano(conn, idManoFiltro, mano);
          } else {
              System.out.println("La mano no cumple con la condición del filtro: "+ tipoFiltro);
          }

          conn.commit();
      } catch (SQLException e) {
          e.printStackTrace();
      }
  }
  private static int obtenerOInsertarFiltro(Connection conn, String tipoFiltro) throws SQLException {

	    String sqlBuscarFiltro="SELECT id FROM Filtro WHERE codigo = ?";
	    try (PreparedStatement pstmtBuscarFiltro=conn.prepareStatement(sqlBuscarFiltro)) {
	        pstmtBuscarFiltro.setString(1, tipoFiltro);
	        ResultSet resultSet= pstmtBuscarFiltro.executeQuery();

	        if (resultSet.next()) {


	            return resultSet.getInt("id");
	        } else {
	            return insertarFiltro(conn, tipoFiltro, "Texto explicativo para "+ tipoFiltro);
	        }
	    }
	}
  
  private static int obtenerOInsertarManoFiltro(Connection conn, int idFiltro, ArrayList<Carta> mano) throws SQLException {
     
      String sqlBuscarManoFiltro = "SELECT id FROM ManoFiltro WHERE idFiltro = ?";
      try (PreparedStatement pstmtBuscarManoFiltro= conn.prepareStatement(sqlBuscarManoFiltro)) {
          pstmtBuscarManoFiltro.setInt(1, idFiltro);
          ResultSet resultSet=pstmtBuscarManoFiltro.executeQuery();

          while (resultSet.next()) {
              int idManoFiltro= resultSet.getInt("id");
              if (manoFiltroContieneCartas(conn,idManoFiltro, mano)) {
                  return idManoFiltro;
              }
          }

          return insertarManoFiltro(conn, idFiltro);
      }
  }

  private static void relacionarCartasConMano(Connection conn, int idManoFiltro, ArrayList<Carta> mano) throws SQLException {
      for (Carta carta:mano) {
          int idCarta = obtenerOInsertarCarta(conn,carta);
          relacionarCartaMano(conn, idManoFiltro, idCarta);
      }
  }
  
  private static int obtenerOInsertarCarta(Connection conn, Carta carta) throws SQLException {
      String sqlBuscarCarta="SELECT id FROM Carta WHERE palo = ? AND valor = ?";
      try (PreparedStatement pstmtBuscarCarta=conn.prepareStatement(sqlBuscarCarta)) {
          pstmtBuscarCarta.setString(1, carta.getPalo());
          pstmtBuscarCarta.setInt(2, carta.getValor());
          ResultSet resultSet = pstmtBuscarCarta.executeQuery();

          if (resultSet.next()) {
              return resultSet.getInt("id");
          } else {
              insertarCarta(conn, carta);
              return obtenerUltimoIdGenerado(conn);
          }
      }
  }
	private static boolean manoFiltroContieneCartas(Connection conn, int idManoFiltro, ArrayList<Carta> mano) throws SQLException {
	    String sql = "SELECT idCarta FROM CartaMano WHERE idManoFiltro = ?";
	    try (PreparedStatement pstmt= conn.prepareStatement(sql)) {
	        pstmt.setInt(1, idManoFiltro);
	        ResultSet resultSet= pstmt.executeQuery();
	        ArrayList<Integer> cartasEnMano= new ArrayList<>();
	        while (resultSet.next()) {
	            cartasEnMano.add(resultSet.getInt("idCarta"));
	        }
	        for (Carta carta : mano) {
	            int idCarta= obtenerIdCarta(conn, carta);
	            if (!cartasEnMano.contains(idCarta)) {
	                return false;
	            }
	        }
	        return true;
	    }
	}

  private static int insertarFiltro(Connection conn, String codigo, String textoExplicativo) throws SQLException {
      String sql= "INSERT INTO Filtro (codigo, textoExplicativo) VALUES (?, ?)";
      try (PreparedStatement pstmt= conn.prepareStatement(sql)) {
          pstmt.setString(1, codigo);
          pstmt.setString(2, textoExplicativo);
          pstmt.executeUpdate();
          return obtenerUltimoIdGenerado(conn);
      }
  }
  private static int insertarManoFiltro(Connection conn, int idFiltro) throws SQLException {
      String sql= "INSERT INTO ManoFiltro (idFiltro) VALUES (?)";
      try (PreparedStatement pstmt= conn.prepareStatement(sql)) {
          pstmt.setInt(1, idFiltro);
          pstmt.executeUpdate();
          return obtenerUltimoIdGenerado(conn);
      }
  }
  private static void insertarCarta(Connection conn, Carta carta) throws SQLException {
      String sql= "INSERT OR IGNORE INTO Carta (palo, valor) VALUES (?, ?)";
      try (PreparedStatement pstmt= conn.prepareStatement(sql)) {
          pstmt.setString(1, carta.getPalo());
          pstmt.setInt(2, carta.getValor());
          pstmt.executeUpdate();
      }
  }
  private static int obtenerIdCarta(Connection conn, Carta carta) throws SQLException {
      String sql= "SELECT id FROM Carta WHERE palo = ? AND valor = ?";
      try (PreparedStatement pstmt= conn.prepareStatement(sql)) {
          pstmt.setString(1, carta.getPalo());
          pstmt.setInt(2, carta.getValor());
          return pstmt.executeQuery().getInt("id");
      }
  }
  private static void relacionarCartaMano(Connection conn, int idManoFiltro, int idCarta) throws SQLException {
      String sql= "INSERT INTO CartaMano (idManoFiltro, idCarta) VALUES (?, ?)";
      try (PreparedStatement pstmt= conn.prepareStatement(sql)) {
          pstmt.setInt(1, idManoFiltro);
          pstmt.setInt(2, idCarta);
          pstmt.executeUpdate();
      }
  }
  private static int obtenerUltimoIdGenerado(Connection conn) throws SQLException {
      try (PreparedStatement pstmt= conn.prepareStatement("SELECT last_insert_rowid()")) {
          return pstmt.executeQuery().getInt(1);
      }
  }
 
  private static boolean cumpleCondicion(ArrayList<Carta> mano, String tipoFiltro) {
      switch (tipoFiltro) {
          case "Poker":
              return tienePoker(mano);
          case "Full":
              return tieneFull(mano);
        
          default:
              return false;
      }
  }
  private static boolean tienePoker(ArrayList<Carta> mano) {

	    for (int i = 0; i< mano.size(); i++){
	        int count = 1;
	        for (int j = i + 1; j<mano.size(); j++) {
	            if (mano.get(i).getValor()== mano.get(j).getValor()){
	                count++;
	            }
	        }
	        if (count==4) {
	            return true;
	        }
	    }
	    return false;
	}
	private static boolean tieneFull(ArrayList<Carta> mano) {

	    boolean tresIguales= false;
	    boolean dosIguales= false;
	    for (int i = 0; i< mano.size()- 2; i++) {
	        int count = 1;
	        for (int j= i+1; j< mano.size(); j++) {
	            if (mano.get(i).getValor()== mano.get(j).getValor()) {
	                count++;
	            }
	        }
	        if (count== 3) {
	            tresIguales = true;
	        } else if (count== 2) {
	            dosIguales= true;
	        }
	    }
	    return tresIguales && dosIguales;
	}
	
}



