import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import javax.swing.*;

public class Client extends JFrame implements ActionListener, KeyListener {
  private static final long serialVersionUID = 1L;
  private JTextArea text;
  private JTextField txtMsg;
  private JButton btnSend;
  private JLabel lblHistory;
  private JLabel lblMsg;
  private JPanel pnlContent;
  private Socket socket;
  private OutputStream ou;
  private Writer ouw;
  private BufferedWriter bfw;
  private JTextField txtIP;
  private JTextField txtPort;
  private JTextField txtName;

  // CLI do chat
  private JLabel lblMessage = new JLabel("Digite o comando desejado:");
  private JTextField command = new JTextField();
  private Object[] texts = {lblMessage, command};
  private String cmdTxt = "";

  public Client() throws IOException {
  }

  /***
  * Método usado para connect no server socket, retorna IO Exception caso dê algum erro.
  * @throws IOException
  */
  public void connect() throws IOException {
    socket = new Socket(txtIP.getText(),Integer.parseInt(txtPort.getText()));
    ou = socket.getOutputStream();
    ouw = new OutputStreamWriter(ou);
    bfw = new BufferedWriter(ouw);

    bfw.write(txtName.getText()+"\r\n");
    bfw.flush();
  }

  /***
  * Método usado para enviar mensagem para o server socket
  * @param msg do tipo String
  * @throws IOException retorna IO Exception caso dê algum erro.
  */
  public void sendMessage(String msg) throws IOException {
    if (msg.equals("/SAIR")) {
      bfw.write("Desconectado \r\n");
      text.append("Desconectado \r\n");
      exit();
    } else {
      if (!msg.equals("")) {
        // Comportamento para o comando /NICk
        if (msg.contains("/NICK")) {
          String newName = msg.split(" ")[1];
          txtName.setText(newName);
          setTitle(txtName.getText());
        }

        bfw.write(msg+"\r\n");
        bfw.flush();
        text.append(txtName.getText() + " diz -> " + txtMsg.getText()+"\r\n");
      }
    }
    txtMsg.setText("");
  }

  /**
   * Método usado para receber mensagem do servidor
   * @throws IOException retorna IO Exception caso dê algum erro.
   */
  public void listen() throws IOException {
    InputStream in = socket.getInputStream();
    InputStreamReader inr = new InputStreamReader(in);
    BufferedReader bfr = new BufferedReader(inr);
    String msg = "";

    // Aguardando uma resposta do servidor
    while (msg == "") {
      if (bfr.ready()) {
        msg = bfr.readLine();
      }
    }

    if (msg.equals("Usuário já cadastrado!")) {
      JOptionPane.showMessageDialog(null, msg);
    } else {
      // Painel do chat
      pnlContent = new JPanel();
      text = new JTextArea(10,20);
      text.setEditable(false);
      text.setBackground(new Color(240,240,240));
      txtMsg = new JTextField(20);
      lblHistory = new JLabel("Histórico");
      lblMsg = new JLabel("Mensagem");
      btnSend = new JButton("Enviar");
      btnSend.setToolTipText("Enviar Mensagem");
      btnSend.addActionListener(this);
      btnSend.addKeyListener(this);
      txtMsg.addKeyListener(this);
      JScrollPane scroll = new JScrollPane(text);
      text.setLineWrap(true);
      pnlContent.add(lblHistory);
      pnlContent.add(scroll);
      pnlContent.add(lblMsg);
      pnlContent.add(txtMsg);
      pnlContent.add(btnSend);
      pnlContent.setBackground(Color.LIGHT_GRAY);
      text.setBorder(BorderFactory.createEtchedBorder(Color.BLUE,Color.BLUE));
      txtMsg.setBorder(BorderFactory.createEtchedBorder(Color.BLUE, Color.BLUE));
      setTitle(txtName.getText());
      setContentPane(pnlContent);
      setLocationRelativeTo(null);
      setResizable(false);
      setSize(250,300);
      setVisible(true);
      setDefaultCloseOperation(EXIT_ON_CLOSE);

      // Ouvindo o servidor
      while (!"Sair".equalsIgnoreCase(msg)) {
        if (bfr.ready()) {
          msg = bfr.readLine();
          if (msg.equals("Sair")) {
            text.append("Servidor caiu! \r\n");
          } else {
            text.append(msg + "\r\n");
          }
        }
      }
    }
  }

/***
  * Método usado quando o usuário clica em sair
  * @throws IOException retorna IO Exception caso dê algum erro.
  */
  public void exit() throws IOException {
    // sendMessage("Sair");
    bfw.close();
    ouw.close();
    ou.close();
    socket.close();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    try {
      if (e.getActionCommand().equals(btnSend.getActionCommand())) {
        sendMessage(txtMsg.getText());
      }
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }

  @Override
  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_ENTER){
      try {
        sendMessage(txtMsg.getText());
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }
  }
  
  @Override
  public void keyReleased(KeyEvent arg0) {
  }
  
  @Override
  public void keyTyped(KeyEvent arg0) {
  }

  public static void main(String []args) throws IOException{
    Client client = new Client();

    do {
      client.command.setText("");
      JOptionPane.showMessageDialog(null, client.texts);
      client.cmdTxt = client.command.getText();

      if (client.cmdTxt.equals("/ENTRAR")) {
        // Messagem para inserir informações do servidor
        JLabel lblMsg = new JLabel("Verificar!");
        client.txtIP = new JTextField("127.0.0.1");
        client.txtPort = new JTextField("12345");
        client.txtName = new JTextField("Cliente");
        Object[] txts = {lblMsg, client.txtIP, client.txtPort, client.txtName };
        JOptionPane.showMessageDialog(null, txts);

        // Tentando connect com o nome de cliente passado pelo usuário
        client.connect();
        // Ouvindo as respostas do servidor
        client.listen();
      }

    } while (!client.cmdTxt.equals("/SAIR"));
  }
}
