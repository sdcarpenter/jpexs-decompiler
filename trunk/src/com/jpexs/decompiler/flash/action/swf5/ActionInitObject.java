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
package com.jpexs.decompiler.flash.action.swf5;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.treemodel.InitObjectTreeItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ActionInitObject extends Action {

   public ActionInitObject() {
      super(0x43, 0);
   }

   @Override
   public String toString() {
      return "InitObject";
   }

   @Override
   public void translate(Stack<GraphTargetItem> stack, List<GraphTargetItem> output, java.util.HashMap<Integer, String> regNames) {
      long numArgs = popLong(stack);
      List<GraphTargetItem> values = new ArrayList<GraphTargetItem>();
      List<GraphTargetItem> names = new ArrayList<GraphTargetItem>();
      for (long l = 0; l < numArgs; l++) {
         values.add(stack.pop());
         names.add(stack.pop());
      }
      stack.push(new InitObjectTreeItem(this, names, values));
   }
}