package seda_project.control_alt_defeat.gamebox.Memory.Controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import seda_project.control_alt_defeat.gamebox.network.Lan;
import seda_project.control_alt_defeat.gamebox.network.LanClient;
import seda_project.control_alt_defeat.gamebox.network.NetworkLayer;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class JoinLan extends Controller implements Initializable {
    @FXML
    private Label joinStatus;

    @FXML
    private TextField joinPlayerNameTF, ipAdresseTF;

    /**
     * Handles the action of going back from the JoinLan scene. It plays a button sound effect, clears the current
     * session, and transitions back to the previous scene. This method is typically called when the user clicks the
     * "Back" button in the user interface, allowing them to return to the previous screen without joining a LAN game.
     */
    @FXML
    protected void onBackAction(){
        sC.play("button");
        Session.clear();
        c.backScene(header,vS);
    }

    /**
     * Initializes the JoinLan controller by setting up the user interface elements, including hiding the join status
     * label initially. This method is called automatically when the JoinLan scene is loaded, ensuring that the user
     * interface is properly configured before user interaction.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        joinStatus.setVisible(false);
    }

    /**
     * Handles the action of connecting to a host in a LAN game. It retrieves the player's name and IP address from the
     * input fields, validates the name length and IP address format, and if valid, attempts to connect to the specified
     * host. If the connection is successful, it sets up the session with the player's data, marks the player as a
     * client, and transitions to the WaitForOpponent scene. If any validation fails or the connection cannot be
     * established, it displays an appropriate error message in the join status label and plays an error sound.
     */
    @FXML
    protected void onConnectAction(){
        sC.play("button");
        String yourName = c.checkNameInput(joinPlayerNameTF.getText(),2);
        if (c.checkNameLength(yourName,2,joinStatus)) {
            if (checkIP()) {
                try {
                    NetworkLayer layer = connectToHost(ipAdresseTF.getText());

                    Session s = Session.current();
                    s.myName  = yourName;
                    s.isHost  = false;
                    s.network = layer;


                    WaitForOpponent controller = (WaitForOpponent) c.changeScene("/Views/Memory/WaitForOpponent.fxml",header,vS);
                    controller.passJoinData(yourName, ipAdresseTF.getText());
                } catch (Exception e) {
                    joinStatus.setVisible(true);
                    joinStatus.setText("Could not connect: " + e.getMessage());
                    sC.play("error");
                }
            }
        }
        else {
            sC.play("error");
        }
    }

    /**
     * Attempts to connect to a host in a LAN game using the provided IP address. It creates a new NetworkLayer
     * instance by joining the specified host and returns the established connection. If the connection cannot be
     * established, it throws an exception with an appropriate error message.
     * @param ipAddress The IP address of the host to connect to. This parameter is used to identify the target host in
     *                  the LAN network for establishing a connection.
     * @return A NetworkLayer instance representing the established connection to the host. This object is used for
     *         communication between the client and the host in the LAN game.
     * @throws Exception If the connection to the host cannot be established, an exception is thrown with a message
     * indicating the reason for the failure. This allows the calling method to handle connection errors appropriately.
     */
    private NetworkLayer connectToHost(String ipAddress) throws Exception {
        return LanClient.join(ipAddress, Lan.DEFAULT_PORT);
    }


    /**
     * Validates the IP address entered by the user in the ipAdresseTF TextField. It checks if the input is empty,
     * if it has the correct format (four parts separated by dots), and if each part is within the valid range of 0 to
     * 255. If any validation fails, it displays an appropriate error message in the joinStatus label and plays an error
     * sound. If all validations pass, it returns true, indicating that the IP address is valid.
     * @return true if the IP address is valid, false otherwise. This return value is used to determine whether to
     * proceed with the connection attempt in the onConnectAction method.
     */
    private boolean checkIP() {
        joinStatus.setVisible(false);
        String ipAdresse = ipAdresseTF.getText();
        if (ipAdresse.isEmpty()){
            sC.play("error");
            joinStatus.setVisible(true);
            joinStatus.setText("You need to fill in an IP-Address");
            return false;
        }
        else {
            String[] ipParts = ipAdresse.split("\\.");
            if (ipParts.length != 4){
                System.out.println(ipParts.length);
                joinStatus.setVisible(true);
                joinStatus.setText("You need to fill in a correct IP-Address");
                sC.play("error");
                return false;
            }

            for (String s : ipParts){
                int number = Integer.parseInt(s);
                if (number<0||number >255){
                    joinStatus.setVisible(true);
                    joinStatus.setText("The numbers of your IP-Address can only be in the range of 0 to 255!");
                    sC.play("error");
                    return false;
                }
            }
            if (ipAdresse.endsWith(".")){
                joinStatus.setVisible(true);
                joinStatus.setText("Your IP-Address may not end with a dot!");
                sC.play("error");
                return false;
            }
        }
        return true;
    }

    /**
     * Transfers data back to the JoinLan controller when returning from another scene, such as the WaitForOpponent
     * scene. It sets the player's name and IP address based on the provided parameters, ensuring that the user
     * interface reflects the previously entered values. This method is typically called when navigating back to the
     * JoinLan scene, allowing users to see their previous input and make adjustments if necessary.
     * @param joinName The name of the joining player, which is set in the joinPlayerNameTF TextField to reflect the
     *                 player's input or previous selection.
     * @param ipAddress The IP address of the host to join, which is set in the ipAdresseTF TextField to reflect the
     *                  player's input or previous selection. This allows users to see the IP address they previously entered when returning to the JoinLan scene.
     */
    public void backTransfer(String joinName, String ipAddress){
        joinPlayerNameTF.setText(joinName);
        ipAdresseTF.setText(ipAddress);
    }
}
