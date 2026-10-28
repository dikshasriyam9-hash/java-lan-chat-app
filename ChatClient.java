import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

/**
 * ChatClient - run this on every user's computer.
 * Simple Swing window: message area, text box, Send button.
 */
public class ChatClient extends JFrame {

    private final JTextArea chatArea = new JTextArea();
    private final JTextField inputField = new JTextField();
    private final JButton sendButton = new JButton("Send");
    private PrintWriter out;

    public ChatClient(String host, String username) throws IOException {
        super("LAN Chat - " + username);

        // ---- GUI ----
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        bottom.add(inputField, BorderLayout.CENTER);
        bottom.add(sendButton, BorderLayout.EAST);
        bottom.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        add(new JScrollPane(chatArea), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        setSize(450, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ---- Network ----
        Socket socket = new Socket(host, 5000);
        out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out.println(username);                       // first message = username

        // thread that keeps reading messages from the server
        new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    final String m = msg;
                    SwingUtilities.invokeLater(() -> {
                        chatArea.append(m + "\n");
                        chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    });
                }
            } catch (IOException e) { /* connection closed */ }
            SwingUtilities.invokeLater(() -> chatArea.append("** Disconnected from server **\n"));
        }).start();

        // send on button click or Enter key
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (!text.isEmpty()) {
            out.println(text);
            inputField.setText("");
        }
    }

    public static void main(String[] args) {
        String host = JOptionPane.showInputDialog("Enter Server IP (same Wi-Fi/LAN):", "localhost");
        if (host == null) return;
        String name = JOptionPane.showInputDialog("Enter your name:");
        if (name == null || name.trim().isEmpty()) return;

        try {
            new ChatClient(host.trim(), name.trim()).setVisible(true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Cannot connect to server: " + e.getMessage());
        }
    }
}
