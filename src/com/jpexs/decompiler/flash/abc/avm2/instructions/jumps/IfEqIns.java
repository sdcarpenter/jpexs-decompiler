/*
 *  Copyright (C) 2010-2014 JPEXS
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
package com.jpexs.decompiler.flash.abc.avm2.instructions.jumps;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.IfTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.EqAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.NeqAVM2Item;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.graph.GraphTargetItem;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class IfEqIns extends InstructionDefinition implements IfTypeIns {

    public IfEqIns() {
        super(0x13, "ifeq", new int[]{AVM2Code.DAT_OFFSET});
    }

    @Override
    public void translate(boolean isStatic, int scriptIndex, int classIndex, HashMap<Integer, GraphTargetItem> localRegs, Stack<GraphTargetItem> stack, Stack<GraphTargetItem> scopeStack, ConstantPool constants, AVM2Instruction ins, List<MethodInfo> method_info, List<GraphTargetItem> output, MethodBody body, ABC abc, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames, String path, HashMap<Integer, Integer> localRegsAssignmentIps, int ip, HashMap<Integer, List<Integer>> refs, AVM2Code code) {
        GraphTargetItem v2 = stack.pop();
        GraphTargetItem v1 = stack.pop();
        stack.push(new EqAVM2Item(ins, v1, v2));
    }

    @Override
    public void translateInverted(HashMap<Integer, GraphTargetItem> localRegs, Stack<GraphTargetItem> stack, AVM2Instruction ins) {
        GraphTargetItem v2 = stack.pop();
        GraphTargetItem v1 = stack.pop();
        stack.push(new NeqAVM2Item(ins, v1, v2));
    }

    @Override
    public int getStackDelta(AVM2Instruction ins, ABC abc) {
        return -2;
    }
}
