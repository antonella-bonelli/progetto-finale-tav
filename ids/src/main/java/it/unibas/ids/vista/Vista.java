package it.unibas.ids.vista;

import com.google.inject.Inject;
import it.unibas.ids.Costanti;
import it.unibas.ids.controllo.IControllo;

import javax.swing.*;
import java.awt.*;

public class Vista extends JFrame implements IVista {
    @Inject
    private IControllo controllo;
    @Inject
    private IMainView mainView;

    public void init() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("IDS");
        createMenu();
        ((JPanel)this.getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.getContentPane().add((JPanel)mainView);
        mainView();
    }

    public void mainView() {
        //this.setSize(new Dimension(620, 640));

        this.mainView.setButtonAction(Costanti.AZIONE_START, this.controllo.getAction(Costanti.AZIONE_START));
        this.mainView.setButtonAction(Costanti.AZIONE_STOP, this.controllo.getAction(Costanti.AZIONE_STOP));

        // Massimizza la finestra
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);

        this.setVisible(true);

        this.setLocationRelativeTo(null);
    }

    private void createMenu() {
        JMenuBar barraMenu = new JMenuBar();
        this.setJMenuBar(barraMenu);
        createFileMenu(barraMenu);
    }

    private void createFileMenu(JMenuBar barraMenu) {
        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic(java.awt.event.KeyEvent.VK_F);
        barraMenu.add(menuFile);
        JMenuItem voceEsci = new JMenuItem(this.controllo.getAction(Costanti.AZIONE_ESCI));
        JMenuItem voceStart = new JMenuItem(this.controllo.getAction(Costanti.AZIONE_START));
        JMenuItem voceClone = new JMenuItem(this.controllo.getAction(Costanti.AZIONE_CLONE));
        menuFile.add(voceStart);
        menuFile.add(voceClone);
        menuFile.add(voceEsci);
    }

    public void errorDialog(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio, "ERRORE", JOptionPane.ERROR_MESSAGE);
    }
}
