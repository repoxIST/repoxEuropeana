/*
 * Viewer.java
 *
 * Created on 4 de Janeiro de 2002, 14:33
 */

package pt.utl.ist.marc;

import pt.utl.ist.marc.iso2709.IsoNavigator;

import javax.swing.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
/** Utility to display the records in a ISO 2709 file. 
 *
 * @author  Nuno Freire
 */
public class Viewer extends javax.swing.JFrame {

    IsoNavigator iso;

    /** Creates new form Viewer */
    public Viewer(String filename) {
        initComponents();
        openIso(filename);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        viewArea = new javax.swing.JEditorPane();
        bNext = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();

        setTitle("Marc Viewer");
        setName("main");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel1.setMinimumSize(new java.awt.Dimension(480, 300));
        jPanel1.setPreferredSize(new java.awt.Dimension(480, 300));
        viewArea.setBorder(null);
        viewArea.setEditable(false);
        viewArea.setContentType("text/ascii");
        viewArea.setMargin(new java.awt.Insets(1, 1, 1, 1));
        jScrollPane1.setViewportView(viewArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 2, 2);
        jPanel1.add(jScrollPane1, gridBagConstraints);

        bNext.setText("Seguinte");
        bNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bNextActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel1.add(bNext, gridBagConstraints);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        fileMenu.setText("Ficheiro");
        openMenuItem.setText("Abrir...");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(openMenuItem);

        exitMenuItem.setText("Sair");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setText("Editar");
        editMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editMenuActionPerformed(evt);
            }
        });

        cutMenuItem.setText("Cortar");
        cutMenuItem.setEnabled(false);
        editMenu.add(cutMenuItem);

        copyMenuItem.setText("Copiar");
        copyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyMenuItemActionPerformed(evt);
            }
        });

        editMenu.add(copyMenuItem);

        pasteMenuItem.setText("Colar");
        pasteMenuItem.setEnabled(false);
        editMenu.add(pasteMenuItem);

        menuBar.add(editMenu);

        setJMenuBar(menuBar);

        pack();
    }//GEN-END:initComponents

    private void copyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyMenuItemActionPerformed
        Clipboard clipboard = getToolkit ().getSystemClipboard ();
        StringSelection sel=new StringSelection (viewArea.getSelectedText());
        clipboard.setContents(sel,sel);
    }//GEN-LAST:event_copyMenuItemActionPerformed

    private void editMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editMenuActionPerformed
    }//GEN-LAST:event_editMenuActionPerformed

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        final JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(Viewer.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            openIso(file.getAbsolutePath());
        }
    }//GEN-LAST:event_openMenuItemActionPerformed

    private void bNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bNextActionPerformed
        gotoNextRecord();
    }//GEN-LAST:event_bNextActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bNext;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JEditorPane viewArea;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu editMenu;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables


    /**************************************************************************
     ************                  Private Methods           ******************
     *************************************************************************/
    private void gotoNextRecord(){
        for (Record record : iso.getNextRecords()) {
            viewArea.setText(record.toString());
        }
    }

    private void openIso(String filename){
        if (new File(filename).exists()){
            if (iso!=null)
                iso.close();
            iso=new IsoNavigator(filename,1);
            gotoNextRecord();
        }
    }
}
