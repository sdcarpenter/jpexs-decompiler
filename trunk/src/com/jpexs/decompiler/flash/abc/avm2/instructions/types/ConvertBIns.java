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
package com.jpexs.decompiler.flash.abc.avm2.instructions.types;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.ConvertTreeItem;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ConvertBIns extends InstructionDefinition implements CoerceOrConvertTypeIns {

   public ConvertBIns() {
      super(0x76, "convert_b", new int[]{});
   }

   @Override
   public void execute(LocalDataArea lda, ConstantPool constants, List arguments) {
      Object value = lda.operandStack.pop();
      boolean bval;
      if (value instanceof Boolean) {
         bval = (Boolean) value;
      } else if (value instanceof Long) {
         bval = ((Long) value).longValue() != 0;
      } else if (value instanceof String) {
         bval = !((String) value).equals("");
      } else {
         bval = true;
      }
      lda.operandStack.push((Boolean) bval);
   }

   @Override
   public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, GraphTargetItem> localRegs, Stack<GraphTargetItem> stack, java.util.Stack<GraphTargetItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<GraphTargetItem> output, com.jpexs.decompiler.flash.abc.types.MethodBody body, com.jpexs.decompiler.flash.abc.ABC abc, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      stack.push(new ConvertTreeItem(ins, (GraphTargetItem) stack.pop(), getTargetType(constants, ins, fullyQualifiedNames)));
   }

   @Override
   public int getStackDelta(AVM2Instruction ins, ABC abc) {
      return -1 + 1;
   }

   public String getTargetType(ConstantPool constants, AVM2Instruction ins, List<String> fullyQualifiedNames) {
      return "Boolean";
   }
}