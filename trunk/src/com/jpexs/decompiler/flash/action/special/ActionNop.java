/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.action.special;

import com.jpexs.decompiler.flash.action.Action;
import java.util.List;
import java.util.Stack;

public class ActionNop extends Action {

   public ActionNop() {
      super(-1, 0);
   }

   @Override
   public String toString() {
      return "Nop";
   }

   @Override
   public void translate(Stack<com.jpexs.decompiler.flash.graph.GraphTargetItem> stack, List<com.jpexs.decompiler.flash.graph.GraphTargetItem> output, java.util.HashMap<Integer, String> regNames) {
      //output.add(new SimpleActionTreeItem(this, "nop();"));
   }
}