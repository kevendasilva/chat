import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class Server extends Thread {
  private static ArrayList<BufferedWriter> clients;
  private static ServerSocket server;
  private String name; 
  private Socket con;
  private InputStream in;
  private InputStreamReader inr;
  private BufferedReader bfr;
  private static ArrayList<String> clientsNames;


  public Server(Socket con) {
    this.con = con;
    try {
      in  = con.getInputStream();
      inr = new InputStreamReader(in);
      bfr = new BufferedReader(inr);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // Método executado sempre o que o cliente conecta no servidor
  public void run() {
    try {
      String message;
      OutputStream ou =  this.con.getOutputStream();
      Writer ouw = new OutputStreamWriter(ou);
      BufferedWriter bfw = new BufferedWriter(ouw);
      clients.add(bfw);
      name = message = bfr.readLine();

      if (clientsNames.contains(name)) {
        bfw.write("Usuário já cadastrado!\r\n");
        bfw.flush();
      } else {
        clientsNames.add(name);

        bfw.write("Usuário conectado!\r\n");
        bfw.flush();

        while (!"/SAIR".equalsIgnoreCase(message) && message != null) {
          message = bfr.readLine();
          if (message.contains("/NICK")) {
            // O novo nick vem logo depois do comando
            String newName = message.split(" ")[1];

            if (clientsNames.contains(newName)) {
              message = "Já existe um usuário com o nick: " + newName;

              bfw.write(message);
              bfw.flush();
            } else {
              // Removendo o nome antigo
              clientsNames.remove(name);

              // Adicionando o novo nome
              clientsNames.add(newName);
              message = name + " agora passa a ser chamar: " + newName;
              name = newName;

              sendToAll(bfw, message);
            }
          } else if (message.equals("/USUARIOS")) {
            String listUsers = clientsNames.toString();

            bfw.write("Usuários conectados: " + listUsers + "\r\n");
            bfw.flush();
          } else {
            sendToAll(bfw, message);
          }

          System.out.println(message);
        }
      }

      // Removendo o cliente que saiu da sessão
      clientsNames.remove(name);
      clients.remove(bfw);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Método responsável por enviar a mensagem para todos no chat
  public void sendToAll(BufferedWriter bwSaida, String msg) throws IOException {
    BufferedWriter bwS;

    for (BufferedWriter bw : clients) {
      bwS = (BufferedWriter)bw;
      if (!(bwSaida == bwS)) {
        bw.write(name + " -> " + msg+"\r\n");
        bw.flush();
      }
    }
  }

  public static void main(String []args) {
    try{
      // Caixa de mensagem
      JLabel lblMessage = new JLabel("Porta do Servidor:");
      JTextField txtPort = new JTextField("12345");
      Object[] texts = {lblMessage, txtPort };
      JOptionPane.showMessageDialog(null, texts);

      //Cria os objetos necessário para instânciar o servidor
      server = new ServerSocket(Integer.parseInt(txtPort.getText()));
      clients = new ArrayList<BufferedWriter>();
      clientsNames = new ArrayList<String>();
      JOptionPane.showMessageDialog(null,"Servidor ativo na porta: " + txtPort.getText());

      while (true) {
        System.out.println("Aguardando conexão...");
        Socket con = server.accept();
        System.out.println("Cliente conectado...");
        Thread t = new Server(con);
        t.start();
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
