package org.yml.plugin.ui;

import org.yml.plugin.util.NotificationUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ConvertToJsonDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonCopy;
    private JButton buttonClose;
    private JTextArea textArea1;

    public static ConvertToJsonDialog instance(String title, String content) {
        final ConvertToJsonDialog dialog = new ConvertToJsonDialog(content);
        dialog.pack();
        dialog.setTitle(title);
        dialog.setLocationRelativeTo(null);
        return dialog;
    }

    public ConvertToJsonDialog(String content) {
        textArea1.setText(content);
        // 是否换行
        textArea1.setLineWrap(true);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonCopy);

        buttonCopy.addActionListener(e -> onCopy());

        buttonClose.addActionListener(e -> onClose());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onClose();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onClose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onCopy() {
        StringSelection selection = new StringSelection(textArea1.getText());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
        NotificationUtils.info("已复制到粘贴板!");
        dispose();
    }

    private void onClose() {
        // add your code here if necessary
        dispose();
    }
}
