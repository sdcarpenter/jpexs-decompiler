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
package com.jpexs.decompiler.flash.abc.avm2.treemodel;

import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.helpers.Helper;
import com.jpexs.decompiler.flash.helpers.Highlighting;
import java.util.HashMap;
import java.util.List;

public abstract class TreeItem extends GraphTargetItem {

   public AVM2Instruction instruction;
   public boolean hidden = false;

   public TreeItem(GraphSourceItem instruction, int precedence) {
      super(instruction, precedence);
   }

   @Override
   public String toString(List localData) {
      return toString((ConstantPool) localData.get(0), (HashMap<Integer, String>) localData.get(1), (List<String>) localData.get(2));
   }

   public abstract String toString(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames);

   public String toStringNoH(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      return Highlighting.stripHilights(toString(constants, localRegNames, fullyQualifiedNames));
   }

   public String toStringSemicoloned(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      return toString(constants, localRegNames, fullyQualifiedNames) + (needsSemicolon() ? ";" : "");
   }

   @Override
   public boolean needsSemicolon() {
      return true;
   }

   /*public String hilight(String str) {
    if (instruction == null) {
    return str;
    }
    if (instruction.mappedOffset >= 0) {
    return Highlighting.hilighOffset(str, instruction.mappedOffset);
    } else {
    return Highlighting.hilighOffset(str, instruction.offset);
    }
    }*/
   public boolean isFalse() {
      return false;
   }

   public boolean isTrue() {
      return false;
   }

   protected String formatProperty(ConstantPool constants, GraphTargetItem object, GraphTargetItem propertyName, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      String obStr = object.toString(Helper.toList(constants, localRegNames, fullyQualifiedNames));
      if (object.precedence > PRECEDENCE_PRIMARY) {
         obStr = "(" + obStr + ")";
      }
      if (object instanceof LocalRegTreeItem) {
         if (((LocalRegTreeItem) object).computedValue instanceof FindPropertyTreeItem) {
            obStr = "";
         }
      }
      if (obStr.equals("")) {
         return propertyName.toString(Helper.toList(constants, localRegNames, fullyQualifiedNames));
      }
      if (propertyName instanceof FullMultinameTreeItem) {
         if (((FullMultinameTreeItem) propertyName).isRuntime()) {
            return joinProperty(obStr, propertyName.toString(Helper.toList(constants, localRegNames, fullyQualifiedNames)));
         } else {
            return joinProperty(obStr, ((FullMultinameTreeItem) propertyName).toString(constants, localRegNames, fullyQualifiedNames));
         }
      } else {
         return obStr + "[" + propertyName.toString(Helper.toList(constants, localRegNames, fullyQualifiedNames)) + "]";
      }
   }

   private String joinProperty(String prefix, String name) {
      if (prefix.endsWith(".")) {
         prefix = prefix.substring(0, prefix.length() - 1);
      }
      if (!Highlighting.stripHilights(name).startsWith("[")) {
         return prefix + "." + name;
      }
      return prefix + name;
   }

   public static String localRegName(HashMap<Integer, String> localRegNames, int reg) {
      if (localRegNames.containsKey(reg)) {
         return localRegNames.get(reg);
      } else {
         if (reg == 0) {
            return "this";
         }
         return "_loc" + reg + "_";
      }
   }
}