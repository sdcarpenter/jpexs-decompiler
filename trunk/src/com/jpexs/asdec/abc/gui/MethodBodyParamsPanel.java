/*
 *  Copyright (C) 2011 JPEXS
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.jpexs.asdec.abc.gui;

import com.jpexs.asdec.abc.types.MethodBody;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.text.NumberFormat;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 *
 * @author JPEXS
 */
public class MethodBodyParamsPanel extends JPanel {

   public JLabel maxStackLabel = new JLabel("Max stack:",SwingConstants.RIGHT);
   public JFormattedTextField maxStackField = new JFormattedTextField(NumberFormat.getNumberInstance());
   public JLabel localCountLabel = new JLabel("Local registers count:",SwingConstants.RIGHT);
   public JFormattedTextField localCountField = new JFormattedTextField(NumberFormat.getNumberInstance());
   public JLabel initScopeDepthLabel = new JLabel("Minimum scope depth:",SwingConstants.RIGHT);
   public JFormattedTextField initScopeDepthField = new JFormattedTextField(NumberFormat.getNumberInstance());
   public JLabel maxScopeDepthLabel = new JLabel("Maximum scope depth:",SwingConstants.RIGHT);
   public JFormattedTextField maxScopeDepthField = new JFormattedTextField(NumberFormat.getNumberInstance());
   public MethodBody body;

   public MethodBodyParamsPanel() {
      setLayout(null);

      maxStackLabel.setBounds(10, 10, 150, 25);
      maxStackField.setBounds(10+150+10, 10, 75, 25);
      add(maxStackLabel);
      add(maxStackField);

      localCountLabel.setBounds(10, 10+30, 150, 25);
      localCountField.setBounds(10+150+10, 10+30, 75, 25);
      add(localCountLabel);
      add(localCountField);

      initScopeDepthLabel.setBounds(10, 10+30+30, 150, 25);
      initScopeDepthField.setBounds(10+150+10, 10+30+30, 75, 25);
      add(initScopeDepthLabel);
      add(initScopeDepthField);

      maxScopeDepthLabel.setBounds(10, 10+30+30+30, 150, 25);
      maxScopeDepthField.setBounds(10+150+10, 10+30+30+30, 75, 25);
      add(maxScopeDepthLabel);
      add(maxScopeDepthField);

      setPreferredSize(new Dimension(300,150));
   }

   public void loadFromBody(MethodBody body) {
      this.body = body;
      if (body == null) {
         maxStackField.setText("0");
         localCountField.setText("0");
         initScopeDepthField.setText("0");
         maxScopeDepthField.setText("0");
         return;
      }
      maxStackField.setText("" + body.max_stack);
      localCountField.setText("" + body.max_regs);
      initScopeDepthField.setText("" + body.init_scope_depth);
      maxScopeDepthField.setText("" + body.max_scope_depth);
   }

   public boolean save() {
      if (body != null) {
         body.max_stack = Integer.parseInt(maxStackField.getText());
         body.max_regs = Integer.parseInt(localCountField.getText());
         body.init_scope_depth = Integer.parseInt(initScopeDepthField.getText());
         body.max_scope_depth = Integer.parseInt(maxScopeDepthField.getText());
         return true;
      }
      return false;
   }
}